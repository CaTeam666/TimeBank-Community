import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Product, ExchangeOrder } from '../types';
import { mallApi } from '../services/mallApi';

interface MallContextType {
  products: Product[];
  orders: ExchangeOrder[];
  loading: boolean;
  createOrder: (userId: string, product: Product, quantity: number) => Promise<boolean>;
  getOrdersByUserId: (userId: string) => ExchangeOrder[];
  fetchUserOrders: (userId: string) => Promise<void>;
  getProductById: (id: string) => Product | undefined;
  loadProducts: (params?: { keyword?: string; category?: string }) => Promise<void>;
}

const MallContext = createContext<MallContextType | undefined>(undefined);

export const MallProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [orders, setOrders] = useState<ExchangeOrder[]>([]);
  const [loading, setLoading] = useState(false);

  // Fetch products on mount
  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async (params?: { keyword?: string; category?: string }) => {
    setLoading(true);
    try {
      const data = await mallApi.getProducts(params);
      setProducts(data);
    } catch (error) {
      console.error('Failed to load products:', error);
    } finally {
      setLoading(false);
    }
  };

  const createOrder = async (userId: string, product: Product, quantity: number): Promise<boolean> => {
    try {
      setLoading(true);
      const res = await mallApi.exchangeProduct({
        userId,
        productId: product.id,
        quantity
      });

      if (res && res.orderNo) {
        // Optimistic update or refresh? For now, we manually create a local order to show in history
        // ideally we fetch orders from API, but for immediate feedback:
        const newOrder: ExchangeOrder = {
          id: res.orderNo, // Use orderNo as ID for now
          userId,
          productId: product.id,
          productName: product.name,
          productImage: product.image,
          price: product.price,
          quantity,
          totalPrice: product.price * quantity,
          status: 'pending_pickup',
          redemptionCode: res.verifyCode,
          createdAt: Date.now()
        };
        setOrders(prev => [newOrder, ...prev]);

        // Refresh products to update stock
        loadProducts();

        return true;
      }
      return false;
    } catch (error) {
      console.error('Exchange failed:', error);
      return false; // Indicate failure
    } finally {
      setLoading(false);
    }
  };

  const getOrdersByUserId = (userId: string) => {
    return orders.filter(o => o.userId === userId);
  };

  const fetchUserOrders = async (userId: string) => {
    setLoading(true);
    try {
      const data = await mallApi.getUserOrders(userId);
      // Merge with existing orders or replace? Replacing for the user is safer to avoid dupes, but we have a single global list.
      // We will filter out old orders for this user and append new ones.
      setOrders(prev => {
        const otherOrders = prev.filter(o => o.userId !== userId);
        return [...otherOrders, ...data];
      });
    } catch (error) {
      console.error('Failed to fetch user orders:', error);
    } finally {
      setLoading(false);
    }
  };

  const getProductById = (id: string) => products.find(p => p.id === Number(id));

  return (
    <MallContext.Provider value={{ products, orders, loading, createOrder, getOrdersByUserId, fetchUserOrders, getProductById, loadProducts }}>
      {children}
    </MallContext.Provider>
  );
};

export const useMall = () => {
  const context = useContext(MallContext);
  if (!context) {
    throw new Error('useMall must be used within a MallProvider');
  }
  return context;
};

