export type ProductStatus = 'ACTIVE' | 'INACTIVE' | 'DRAFT';

export interface RawProduct {
  id?: string;
  productId?: string;
  name: string;
  slug: string;
  description?: string;
  shortDescription?: string;
  image?: string;
  imageUrl?: string;
  brandId?: string;
  brandName?: string;
  categoryId?: string;
  categoryName?: string;
  price?: number;
  basePrice?: number;
  effectivePrice?: number;
  discountedPrice?: number;
  currency?: string;
  rating?: number;
  averageRating?: number;
  reviewCount?: number;
  stockQuantity?: number;
  availableQuantity?: number;
  stockStatus?: string;
  status?: ProductStatus;
  hasDiscount?: boolean;
  promotionBadge?: string;
  discountPercentage?: number;
  brand?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductCardModel {
  id: string;
  productId: string;
  name: string;
  slug: string;
  imageUrl: string;
  brand?: string;
  category?: string;
  price: number;
  originalPrice?: number;
  rating: number;
  reviewCount: number;
  stockQuantity: number;
  stockStatus: string;
  hasDiscount: boolean;
  promotionBadge?: string;
}

export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string;
  imageUrl?: string;
  icon?: string;
}
