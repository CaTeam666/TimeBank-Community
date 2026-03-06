import { ExchangeOrder } from './types';

export const MOCK_ORDERS: ExchangeOrder[] = [
    {
        id: "MOCK_ORD_001",
        userId: "1001", // Make sure this matches tests if needed, or generic
        productId: 1,
        productName: "东北大米 5kg",
        productImage: "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&q=80&w=200",
        price: 50,
        quantity: 1,
        totalPrice: 50,
        status: 'pending_pickup',
        redemptionCode: "882910",
        createdAt: Date.now() - 86400000 // 1 day ago
    },
    {
        id: "MOCK_ORD_002",
        userId: "1001",
        productId: 2,
        productName: "鲁花花生油 5L",
        productImage: "https://images.unsplash.com/photo-1474631245212-32dc3c8310c6?auto=format&fit=crop&q=80&w=200",
        price: 120,
        quantity: 1,
        totalPrice: 120,
        status: 'completed',
        redemptionCode: "123456",
        createdAt: Date.now() - 172800000 // 2 days ago
    },
    {
        id: "MOCK_ORD_003",
        userId: "1001",
        productId: 3,
        productName: "云南白药牙膏 3支装",
        productImage: "https://images.unsplash.com/photo-1559586616-361e18714958?auto=format&fit=crop&q=80&w=200",
        price: 45,
        quantity: 2,
        totalPrice: 90,
        status: 'cancelled',
        redemptionCode: "000000",
        createdAt: Date.now() - 259200000 // 3 days ago
    }
];
