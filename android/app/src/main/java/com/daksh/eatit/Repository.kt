package com.daksh.eatit

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

interface EatItRepository {
    val customer: StateFlow<Customer?>
    fun catalog(): Flow<Catalog>
    fun orders(): Flow<List<Purchase>>
    suspend fun authenticate(email: String, password: String, name: String?)
    suspend fun resetPassword(email: String)
    suspend fun checkout(lines: List<CartLine>, delivery: Delivery, requestId: String): String
    fun signOut()
}
class DemoRepository(context: Context) : EatItRepository {
    private val prefs = context.getSharedPreferences("demo-orders", Context.MODE_PRIVATE)
    override val customer = MutableStateFlow<Customer?>(Customer("demo", "Food lover", "demo@example.com"))
    private val purchases = MutableStateFlow(readOrders())
    override fun catalog() = flowOf(DemoCatalog)
    override fun orders(): Flow<List<Purchase>> = purchases
    override suspend fun authenticate(email: String, password: String, name: String?) {
        customer.value = Customer("demo", name ?: "Food lover", email)
    }
    override suspend fun resetPassword(email: String) = Unit
    override suspend fun checkout(lines: List<CartLine>, delivery: Delivery, requestId: String): String {
        require(lines.isNotEmpty() && delivery.valid())
        if (purchases.value.none { it.id == requestId }) {
            purchases.value = listOf(Purchase(requestId, cartTotal(lines), "0", delivery.address)) + purchases.value
            prefs.edit().putString("orders", JSONArray().apply {
                purchases.value.forEach { put(JSONObject().put("id", it.id).put("total", it.totalPaise).put("address", it.address)) }
            }.toString()).apply()
        }
        return requestId
    }
    private fun readOrders(): List<Purchase> = runCatching {
        val array = JSONArray(prefs.getString("orders", "[]"))
        List(array.length()) { index -> array.getJSONObject(index).let { Purchase(it.getString("id"), it.getLong("total"), "0", it.getString("address")) } }
    }.getOrDefault(emptyList())
    override fun signOut() { customer.value = null }
}
class FirebaseRepository : EatItRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    override val customer = MutableStateFlow(auth.currentUser?.let { Customer(it.uid, it.displayName.orEmpty(), it.email.orEmpty()) })
    init { auth.addAuthStateListener { customer.value = it.currentUser?.let { user -> Customer(user.uid, user.displayName.orEmpty(), user.email.orEmpty()) } } }
    override fun catalog(): Flow<Catalog> = combine(
        database.getReference("Category").snapshots(), database.getReference("Food").snapshots(),
    ) { categories, foods ->
        Catalog(categories.children.map { Category(it.key!!, it.text("name", "Name")) }, foods.children.mapNotNull {
            val price = parsePrice(it.text("price", "Price")) ?: return@mapNotNull null
            Dish(it.key!!, it.text("name", "Name"), it.text("menuid", "Menuid"), price,
                it.text("image", "Image"), it.text("description", "Description"))
        })
    }
    override fun orders(): Flow<List<Purchase>> {
        val uid = customer.value?.id ?: return flowOf(emptyList())
        return database.getReference("Requests").orderByChild("userId").equalTo(uid).snapshots().map { snapshot ->
            snapshot.children.map { Purchase(it.key!!, it.child("totalPaise").getValue(Long::class.java) ?: 0,
                it.text("status"), it.text("address")) }.reversed()
        }
    }
    override suspend fun authenticate(email: String, password: String, name: String?) {
        if (name == null) auth.signInWithEmailAndPassword(email, password).await()
        else {
            val user = auth.createUserWithEmailAndPassword(email, password).await().user!!
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
            customer.value = Customer(user.uid, name, email)
        }
    }
    override suspend fun resetPassword(email: String) { auth.sendPasswordResetEmail(email).await() }
    override suspend fun checkout(lines: List<CartLine>, delivery: Delivery, requestId: String): String {
        val result = FirebaseFunctions.getInstance().getHttpsCallable("placeOrder").call(mapOf(
            "requestId" to requestId, "name" to delivery.name, "phone" to delivery.phone, "address" to delivery.address,
            "items" to lines.map { mapOf("id" to it.dish.id, "quantity" to it.quantity) },
        )).await()
        return (result.data as Map<*, *>)["orderId"] as String
    }
    override fun signOut() = auth.signOut()
}
private fun DataSnapshot.text(vararg keys: String): String = keys.firstNotNullOfOrNull { child(it).value?.toString() }.orEmpty()
private fun Query.snapshots(): Flow<DataSnapshot> = callbackFlow {
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) { trySend(snapshot) }
        override fun onCancelled(error: DatabaseError) { close(error.toException()) }
    }
    addValueEventListener(listener)
    awaitClose { removeEventListener(listener) }
}
/** Cart is local, namespaced by authenticated user, and contains no password or payment data. */
class CartStore(context: Context) {
    private val prefs = context.getSharedPreferences("compose-cart", Context.MODE_PRIVATE)
    fun read(user: String): List<CartLine> = runCatching {
        val json = JSONArray(prefs.getString(user, "[]"))
        List(json.length()) { index -> json.getJSONObject(index).let {
            CartLine(Dish(it.getString("id"), it.getString("name"), it.getString("category"), it.getLong("price"), it.optString("image")), it.getInt("quantity"))
        } }
    }.getOrDefault(emptyList())
    fun write(user: String, lines: List<CartLine>) {
        val json = JSONArray()
        lines.forEach { json.put(JSONObject().put("id", it.dish.id).put("name", it.dish.name)
            .put("category", it.dish.categoryId).put("price", it.dish.pricePaise).put("image", it.dish.image).put("quantity", it.quantity)) }
        prefs.edit().putString(user, json.toString()).apply()
    }
}
