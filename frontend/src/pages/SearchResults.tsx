import React from 'react';
import { ChevronLeft, ChevronRight, Search as SearchIcon } from 'lucide-react';
import { useSearchParams } from 'react-router-dom';
import { useSearch, useSearchFacets } from '../features/search/hooks/useSearch';
import { SearchResultCard } from '../features/search/components/SearchResultCard';
import { FacetSidebar } from '../features/search/components/FacetSidebar';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { mapToProductCard } from '../features/catalog/utils/productMapper';

const SearchResults: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const params = {
    q: searchParams.get('q') || undefined,
    categoryId: searchParams.get('categoryId') || undefined,
    brandIds: searchParams.get('brandIds') || undefined,
    minPrice: searchParams.get('minPrice') ? Number(searchParams.get('minPrice')) : undefined,
    maxPrice: searchParams.get('maxPrice') ? Number(searchParams.get('maxPrice')) : undefined,
    minRating: searchParams.get('minRating') ? Number(searchParams.get('minRating')) : undefined,
    sort: searchParams.get('sort') || 'relevance',
    page: searchParams.get('page') ? Number(searchParams.get('page')) : 0,
    size: 24,
  };

  const { data, isPending, isError } = useSearch(params);
  const { data: facetsData } = useSearchFacets(params);

  // Safely extract results and metadata from PageResponse or Array
  const results = data && 'content' in data ? data.content : Array.isArray(data) ? data : [];
  const totalElements = (data && 'totalElements' in data ? data.totalElements : results.length) ?? 0;
  const totalPages = (data && 'totalPages' in data ? data.totalPages : 1) ?? 1;
  const page = (data && 'page' in data ? data.page : params.page) ?? 0;

  const facets = facetsData?.facets ?? facetsData;
  const hasFacets = Boolean(facets && (facets.brands?.length > 0 || facets.categories?.length > 0));

  const setSort = (sort: string) => {
    const next = new URLSearchParams(searchParams);
    next.set('sort', sort);
    next.delete('page');
    setSearchParams(next);
  };

  const setPage = (nextPage: number) => {
    const next = new URLSearchParams(searchParams);
    next.set('page', String(nextPage));
    setSearchParams(next);
  };

  const heading = params.q ? `Search results for "${params.q}"` : 'All Products';

  return (
    <div className={`grid gap-8 ${hasFacets ? 'md:grid-cols-[280px_1fr]' : 'grid-cols-1'}`}>
      {hasFacets ? (
        <aside className="hidden md:block">
          <FacetSidebar facets={facets} />
        </aside>
      ) : null}

      <section className="min-w-0 space-y-8">
        <header className="flex flex-col sm:flex-row sm:items-center justify-between gap-6 bg-white p-6 rounded-[2rem] border border-gray-100 shadow-sm">
          <div className="space-y-1">
            <h1 className="text-3xl font-black text-gray-900 tracking-tight leading-none">{heading}</h1>
            <p className="text-sm font-bold text-gray-400 uppercase tracking-widest">
              {isPending ? 'Searching the marketplace...' : `${totalElements} products found`}
            </p>
          </div>

          <div className="flex items-center gap-4">
            <span className="text-[10px] font-black text-gray-400 uppercase tracking-widest whitespace-nowrap">Sort By</span>
            <select
              value={params.sort}
              onChange={(event) => setSort(event.target.value)}
              className="h-12 rounded-2xl border-none bg-gray-50 px-6 text-sm font-bold text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 shadow-inner"
            >
              <option value="relevance">Relevance</option>
              <option value="price_asc">Price: Low to high</option>
              <option value="price_desc">Price: High to low</option>
              <option value="rating_desc">Top rated</option>
            </select>
          </div>
        </header>

        {isError ? (
          <div className="rounded-3xl border-2 border-dashed border-red-200 bg-red-50 p-12 text-center">
            <div className="text-4xl mb-4">⚠️</div>
            <h3 className="text-xl font-black text-red-900 mb-2">Search unavailable</h3>
            <p className="text-red-600 font-medium max-w-sm mx-auto">We encountered an error while searching. Please try again later or adjust your filters.</p>
          </div>
        ) : null}

        {isPending ? (
          <div className="flex flex-col items-center justify-center py-32 space-y-6">
            <Spinner size="lg" />
            <p className="text-sm font-black text-gray-400 uppercase tracking-[0.3em] animate-pulse">Filtering Results</p>
          </div>
        ) : results.length === 0 ? (
          <div className="flex flex-col items-center py-24 text-center bg-gray-50 rounded-[3rem] border border-dashed border-gray-200">
            <div className="bg-white p-8 rounded-full shadow-xl mb-8">
              <SearchIcon className="h-16 w-16 text-gray-200" strokeWidth={3} />
            </div>
            <h2 className="text-3xl font-black text-gray-900 mb-4 tracking-tighter">No Matches Found</h2>
            <p className="text-gray-500 font-medium max-w-md mx-auto mb-10">
              We couldn't find anything matching your current search or filters. Try removing some filters or using different keywords.
            </p>
            <Button variant="primary" onClick={() => setSearchParams({})} className="rounded-2xl px-10 py-4 font-black uppercase">
              Clear All Filters
            </Button>
          </div>
        ) : (
          <div className="space-y-12">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {results.map((product: any) => (
                <SearchResultCard key={product.id} product={mapToProductCard(product)} />
              ))}
            </div>

            {totalPages > 1 ? (
              <footer className="flex flex-col items-center gap-6 pt-12 border-t border-gray-100">
                <div className="flex items-center gap-2">
                   <Button 
                    variant="secondary" 
                    className="rounded-xl w-12 h-12 p-0 flex items-center justify-center shadow-md disabled:shadow-none"
                    onClick={() => setPage(Math.max(0, page - 1))} 
                    disabled={page === 0}
                  >
                    <ChevronLeft className="h-6 w-6" />
                  </Button>
                  
                  <div className="flex gap-2">
                    {[...Array(Math.min(5, totalPages))].map((_, i) => {
                      const pageNum = i; // Simple pagination for demo
                      return (
                        <Button
                          key={pageNum}
                          variant={page === pageNum ? 'primary' : 'ghost'}
                          className={`w-12 h-12 rounded-xl font-black text-lg ${page === pageNum ? 'shadow-lg shadow-blue-200' : ''}`}
                          onClick={() => setPage(pageNum)}
                        >
                          {pageNum + 1}
                        </Button>
                      );
                    })}
                  </div>

                  <Button
                    variant="secondary"
                    className="rounded-xl w-12 h-12 p-0 flex items-center justify-center shadow-md disabled:shadow-none"
                    onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                    disabled={page >= totalPages - 1}
                  >
                    <ChevronRight className="h-6 w-6" />
                  </Button>
                </div>
                <p className="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">
                  Showing Page {page + 1} of {totalPages}
                </p>
              </footer>
            ) : null}
          </div>
        )}
      </section>
    </div>
  );
};

export default SearchResults;
