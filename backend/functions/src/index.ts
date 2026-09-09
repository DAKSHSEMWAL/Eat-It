import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

interface OrderItemPayload {
  id: string;
  quantity: number;
}

interface PlaceOrderPayload {
  requestId: string;
  name: string;
  phone: string;
  address: string;
  items: OrderItemPayload[];
  quoteTotalPaise?: number;
}

export const placeOrder = functions.https.onCall(async (data: PlaceOrderPayload, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required to place an order.");
  }

  const uid = context.auth.uid;
  const { requestId, name, phone, address, items, quoteTotalPaise } = data;

  if (!requestId || typeof requestId !== "string" || requestId.trim().length === 0) {
    throw new functions.https.HttpsError("invalid-argument", "Missing or invalid requestId.");
  }
  if (!name || name.trim().length < 2) {
    throw new functions.https.HttpsError("invalid-argument", "Valid full name is required (minimum 2 characters).");
  }
  if (!phone || !/^\+?[0-9]{10,15}$/.test(phone.trim())) {
    throw new functions.https.HttpsError("invalid-argument", "Valid phone number required (10-15 digits).");
  }
  if (!address || address.trim().length < 10) {
    throw new functions.https.HttpsError("invalid-argument", "Valid delivery address required (minimum 10 characters).");
  }
  if (!Array.isArray(items) || items.length === 0) {
    throw new functions.https.HttpsError("invalid-argument", "Cart cannot be empty.");
  }

  const db = admin.database();

  // 1. Idempotency Check
  const idempotencyRef = db.ref(`Idempotency/${uid}/${requestId}`);
  const existingIdempotency = await idempotencyRef.once("value");
  if (existingIdempotency.exists()) {
    const record = existingIdempotency.val();
    return {
      orderId: record.orderId,
      status: "ALREADY_PROCESSED",
      totalPaise: record.totalPaise,
    };
  }

  // 2. Fetch canonical prices from /Food catalog
  const foodsSnap = await db.ref("Food").once("value");
  const foodsData = foodsSnap.val() || {};

  let computedTotalPaise = 0;
  const legacyFoodsList: Array<{
    productId: string;
    productName: string;
    quantity: string;
    price: string;
    discount: string;
  }> = [];

  for (const item of items) {
    if (!item.id || typeof item.quantity !== "number" || item.quantity <= 0 || item.quantity > 99) {
      throw new functions.https.HttpsError("invalid-argument", `Invalid quantity for item ${item.id}`);
    }

    const dishNode = foodsData[item.id];
    if (!dishNode) {
      return {
        status: "ITEM_UNAVAILABLE",
        itemId: item.id,
      };
    }

    const rawPriceStr = dishNode.price || dishNode.Price || "0";
    const itemPricePaise = Math.round(parseFloat(rawPriceStr) * 100);
    if (isNaN(itemPricePaise) || itemPricePaise < 0) {
      throw new functions.https.HttpsError("internal", `Invalid catalog price for item ${item.id}`);
    }

    const lineTotal = itemPricePaise * item.quantity;
    computedTotalPaise += lineTotal;

    legacyFoodsList.push({
      productId: item.id,
      productName: dishNode.name || dishNode.Name || "Dish",
      quantity: item.quantity.toString(),
      price: (itemPricePaise / 100).toFixed(2),
      discount: "0",
    });
  }

  // 3. Price Quote Verification
  if (typeof quoteTotalPaise === "number" && quoteTotalPaise !== computedTotalPaise) {
    return {
      status: "PRICE_MISMATCH",
      updatedTotalPaise: computedTotalPaise,
    };
  }

  // 4. Create Order & Idempotency Record
  const orderId = `ORD_${Date.now()}_${requestId.substring(0, 8)}`;
  const orderRef = db.ref(`Requests/${orderId}`);

  const orderData = {
    userId: uid,
    name: name.trim(),
    phone: phone.trim(),
    address: address.trim(),
    totalPaise: computedTotalPaise,
    total: `₹${(computedTotalPaise / 100).toFixed(2)}`,
    status: "0", // 0: Placed
    foods: legacyFoodsList,
    createdAt: admin.database.ServerValue.TIMESTAMP,
    idempotencyKey: requestId,
  };

  await orderRef.set(orderData);
  await idempotencyRef.set({
    orderId,
    totalPaise: computedTotalPaise,
    createdAt: admin.database.ServerValue.TIMESTAMP,
  });

  return {
    orderId,
    status: "SUCCESS",
    totalPaise: computedTotalPaise,
  };
});
