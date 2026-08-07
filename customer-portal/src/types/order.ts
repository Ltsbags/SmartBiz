export type OrderStatus =
  | 'PENDING'
  | 'APPROVED'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED';

export interface OrderItem {
  productId: string;
  sku: string;
  name: string;
  quantity: number;
  unitPrice: number;
  taxPercentage: number;
  totalPrice: number;
}

export interface ShippingAddress {
  id: string;
  title: string;
  recipientName: string;
  companyName: string;
  streetAddress: string;
  city: string;
  state: string;
  postalCode: string;
  phone: string;
  isDefault: boolean;
}

export interface B2BOrder {
  id: string;
  orderNumber: string;
  poNumber: string;
  createdAt: string;
  status: OrderStatus;
  items: OrderItem[];
  subtotal: number;
  taxAmount: number;
  shippingFee: number;
  totalAmount: number;
  paymentMethod: 'NET_30' | 'CREDIT_LINE' | 'ONLINE_GATEWAY' | 'BANK_TRANSFER';
  paymentStatus: 'UNPAID' | 'PAID' | 'PARTIAL';
  shippingAddress: ShippingAddress;
  trackingNumber?: string;
  courierName?: string;
  estimatedDelivery?: string;
  notes?: string;
}