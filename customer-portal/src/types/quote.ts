export type QuoteStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'EXPIRED' | 'CONVERTED';

export interface QuoteItem {
  productId: string;
  productName: string;
  sku: string;
  requestedQuantity: number;
  offeredUnitPrice?: number;
  discountPercentage?: number;
  lineTotal?: number;
  notes?: string;
}

export interface B2BQuote {
  id: string;
  quoteNumber: string;
  createdAt: string;
  validUntil: string;
  status: QuoteStatus;
  items: QuoteItem[];
  subtotal: number;
  taxTotal: number;
  grandTotal: number;
  paymentTerms: string;
  notes?: string;
  vendorNotes?: string;
}