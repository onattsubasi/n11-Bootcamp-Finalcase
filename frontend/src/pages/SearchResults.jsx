import { ChevronLeft, ChevronRight, Search as SearchIcon } from 'lucide-react';
import { useSearchParams } from 'react-router-dom';
import { useSearch, useSearchFacets } from '../features/search/hooks/useSearch';
import { SearchResultCard } from '../features/search/components/SearchResultCard';
import { FacetSidebar } from '../features/search/components/FacetSidebar';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';

const SearchResults = () => {
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

  const results = data?.content || data?.items || data || [];
  const totalElements = data?.totalElements ?? results.length;
  const totalPages = data?.totalPages ?? 1;
  const page = data?.page ?? params.page ?? 0;

  const setSort = (sort) => {
    const next = new URLSearchParams(searchParams);
    next.set('sort', sort);
    next.delete('page');
    setSearchParams(next);
  };

  const setPage = (nextPage) => {
    const next = new URLSearchParams(searchParams);
    next.set('page', String(nextPage));
    setSearchParams(next);
  };

  const heading = params.q ? `"${params.q}" search results` : 'All products';

  return (
    <div className="grid gap-6 md:grid-cols-[280px_1fr]">
      <FacetSidebar facets={facetsData?.facets ?? facetsData} />

      <section className="min-w-0 space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-xl font-bold text-gray-900">{heading}</h1>
            <p className="mt-1 text-sm text-gray-500">
              {isPending ? 'Searching...' : `${totalElements} products found`}
            </p>
          </div>

          <select
            value={params.sort}
            onChange={(event) => setSort(event.target.value)}
            className="h-10 rounded-lg border border-gray-300 bg-white px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="relevance">Relevance</option>
            <option value="price_asc">Price: Low to high</option>
            <option value="price_desc">Price: High to low</option>
            <option value="rating_desc">Top rated</option>
          </select>
        </div>

        {isError ? (
          <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-600">
            Search failed.
          </div>
        ) : null}

        {isPending ? (
          <div className="flex items-center justify-center py-20">
            <Spinner />
          </div>
        ) : results.length === 0 ? (
          <div className="flex flex-col items-center py-16 text-center">
            <SearchIcon className="mb-4 h-12 w-12 text-gray-300" />
            <p className="text-lg font-medium text-gray-500">No products found.</p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
              {results.map((product) => (
                <SearchResultCard key={product.id} product={product} />
              ))}
            </div>

            {totalPages > 1 ? (
              <div className="flex items-center justify-center gap-3 pt-6">
                <Button variant="ghost" size="sm" onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0}>
                  <ChevronLeft className="h-4 w-4" /> Prev
                </Button>
                <span className="text-sm text-gray-500">
                  Page {page + 1} / {totalPages}
                </span>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                  disabled={page >= totalPages - 1}
                >
                  Next <ChevronRight className="h-4 w-4" />
                </Button>
              </div>
            ) : null}
          </>
        )}
      </section>
    </div>
  );
};

export default SearchResults;
