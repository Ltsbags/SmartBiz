export interface BulkPriceTier {
  minQuantity: number;
  maxQuantity?: number;
  unitPrice: number;
  discountPercentage: number;
}

export interface B2BProduct {
  id: string;
  sku: string;
  name: string;
  category: string;
  brand: string;
  description: string;
  unit: string;
  basePrice: number;
  taxPercentage: number;
  stockQuantity: number;
  moq: number; // Minimum Order Quantity
  inStock: boolean;
  bulkTiers: BulkPriceTier[];
  imageUrl?: string;
  specifications: Record<string, string>;
}