import { unwrapPage } from '../../../lib/utils/api';
import { RawProduct, ProductCardModel } from '../../../types/product';
import { PageResponse } from '../../../types/api';

export const normalizeProduct = (product: any): RawProduct => {
  if (!product) return product;

  const basePrice = product.basePrice ?? product.originalPrice ?? product.price ?? 0;
  const effectivePrice = product.effectivePrice ?? product.discountedPrice ?? product.price ?? basePrice;

  return {
    ...product,
    id: product.id ?? product.productId,
    productId: product.productId ?? product.id,
    brandName: product.brand ?? product.brandName,
    categoryName: product.category ?? product.categoryName,
    price: effectivePrice,
    discountedPrice: effectivePrice,
    basePrice: basePrice,
    rating: Number(product.rating ?? product.averageRating ?? 0),
    stockQuantity:
      product.stockQuantity ??
      product.stock ??
      product.availableQuantity ??
      (product.stockStatus === 'IN_STOCK' ? 1 : 0),
  };
};

export const mapToProductCard = (product: RawProduct): ProductCardModel => {
  const basePrice = product.basePrice ?? 0;
  const price = product.price ?? 0;

  return {
    id: product.id || '',
    productId: product.productId || '',
    name: product.name || 'Unnamed Product',
    slug: product.slug || '',
    imageUrl: product.imageUrl || product.image || '/placeholder.png',
    brand: product.brandName,
    category: product.categoryName,
    price: price,
    originalPrice: basePrice > price ? basePrice : undefined,
    rating: product.rating || 0,
    reviewCount: product.reviewCount || 0,
    stockQuantity: product.stockQuantity || 0,
    stockStatus: product.stockStatus || (product.stockQuantity && product.stockQuantity > 0 ? 'IN_STOCK' : 'OUT_OF_STOCK'),
    hasDiscount: basePrice > price,
    promotionBadge: product.promotionBadge,
  };
};

export const normalizeProductPage = (payload: any): PageResponse<RawProduct> => {
  const page = unwrapPage<any>(payload);
  const normalizedItems = page.items.map(normalizeProduct);

  return {
    ...page,
    content: normalizedItems,
    items: normalizedItems,
  };
};
