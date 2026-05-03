import { useState } from 'react';
import toast from 'react-hot-toast';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { Badge } from '../components/ui/Badge';
import { formatDateTime } from '../lib/utils/format';
import {
  useAdminSearchDocument,
  useAdminSearchDocuments,
} from '../features/admin/hooks/useAdmin';
import { reindexProductSearch, rebuildSearchIndex } from '../features/admin/api/adminApi';

const AdminSearch = () => {
  const [q, setQ] = useState('');
  const [status, setStatus] = useState('');
  const [selectedProductId, setSelectedProductId] = useState('');
  const [page, setPage] = useState(0);

  const searchParams = {
    page,
    size: 20,
    q: q || undefined,
    status: status || undefined,
  };

  const documentsQuery = useAdminSearchDocuments(searchParams);
  const selectedDocumentQuery = useAdminSearchDocument(selectedProductId || undefined);
  const documents = documentsQuery.data?.items ?? documentsQuery.data?.content ?? documentsQuery.data?.data?.items ?? [];
  const totalPages = documentsQuery.data?.page?.totalPages ?? documentsQuery.data?.totalPages ?? 1;

  const handleReindex = async (productId) => {
    try {
      await reindexProductSearch(productId);
      toast.success('Product reindexed');
      documentsQuery.refetch();
      if (selectedProductId === productId) {
        selectedDocumentQuery.refetch();
      }
    } catch {
      toast.error('Failed to reindex product');
    }
  };

  const handleRebuild = async () => {
    try {
      await rebuildSearchIndex();
      toast.success('Search index rebuild started');
      documentsQuery.refetch();
    } catch {
      toast.error('Failed to rebuild search index');
    }
  };

  return (
    <div className="mx-auto max-w-6xl space-y-6 rounded-xl bg-white p-6 shadow-sm">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Search Admin</h1>
          <p className="mt-1 text-sm text-gray-500">Inspect indexed documents and trigger search maintenance actions.</p>
        </div>
        <Button type="button" variant="secondary" onClick={handleRebuild}>
          Rebuild index
        </Button>
      </div>

      <div className="grid gap-3 md:grid-cols-[1fr_180px_auto]">
        <input
          type="text"
          value={q}
          onChange={(event) => {
            setQ(event.target.value);
            setPage(0);
          }}
          placeholder="Search documents..."
          className="h-11 rounded-lg border border-gray-300 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <input
          type="text"
          value={status}
          onChange={(event) => {
            setStatus(event.target.value);
            setPage(0);
          }}
          placeholder="Status"
          className="h-11 rounded-lg border border-gray-300 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <Button
          type="button"
          variant="primary"
          onClick={() => {
            documentsQuery.refetch();
            setPage(0);
          }}
        >
          Filter
        </Button>
      </div>

      {documentsQuery.isPending ? (
        <div className="flex items-center justify-center py-16">
          <Spinner />
        </div>
      ) : documents.length === 0 ? (
        <div className="rounded-lg border border-dashed border-gray-200 p-8 text-center text-sm text-gray-500">
          No search documents found.
        </div>
      ) : (
        <div className="grid gap-6 xl:grid-cols-[1fr_380px]">
          <div className="overflow-hidden rounded-xl border border-gray-200">
            <table className="w-full text-left text-sm">
              <thead className="bg-gray-50 text-gray-600">
                <tr>
                  <th className="px-4 py-3">Product</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Visible</th>
                  <th className="px-4 py-3">Rating</th>
                  <th className="px-4 py-3">Action</th>
                </tr>
              </thead>
              <tbody>
                {documents.map((document) => (
                  <tr
                    key={document.productId}
                    className="border-t border-gray-100 transition-colors hover:bg-gray-50"
                  >
                    <td className="px-4 py-3">
                      <div className="font-medium text-gray-900">{document.name}</div>
                      <div className="text-xs text-gray-500">{document.productId}</div>
                    </td>
                    <td className="px-4 py-3">
                      <Badge tone={document.status === 'ACTIVE' ? 'success' : 'neutral'}>{document.status}</Badge>
                    </td>
                    <td className="px-4 py-3">{document.visible ? 'Yes' : 'No'}</td>
                    <td className="px-4 py-3">{(document.ratingAverage ?? 0).toFixed(1)} ({document.reviewCount ?? 0})</td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2">
                        <Button type="button" variant="ghost" size="sm" onClick={() => setSelectedProductId(document.productId)}>
                          View
                        </Button>
                        <Button type="button" variant="secondary" size="sm" onClick={() => handleReindex(document.productId)}>
                          Reindex
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {totalPages > 1 ? (
              <div className="flex items-center justify-between border-t border-gray-200 px-4 py-3 text-sm text-gray-500">
                <Button type="button" variant="ghost" size="sm" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={page === 0}>
                  Previous
                </Button>
                <span>Page {page + 1} / {totalPages}</span>
                <Button type="button" variant="ghost" size="sm" onClick={() => setPage((current) => Math.min(totalPages - 1, current + 1))} disabled={page >= totalPages - 1}>
                  Next
                </Button>
              </div>
            ) : null}
          </div>

          <aside className="space-y-4 rounded-xl border border-gray-200 p-4">
            <h2 className="text-lg font-semibold text-gray-900">Document details</h2>
            {selectedProductId ? (
              selectedDocumentQuery.isPending ? (
                <div className="flex items-center justify-center py-12">
                  <Spinner />
                </div>
              ) : selectedDocumentQuery.data?.data ? (
                <div className="space-y-3 text-sm text-gray-600">
                  <div>
                    <p className="text-xs uppercase tracking-wide text-gray-400">Product ID</p>
                    <p className="font-medium text-gray-900">{selectedDocumentQuery.data.data.productId}</p>
                  </div>
                  <div>
                    <p className="text-xs uppercase tracking-wide text-gray-400">Name</p>
                    <p className="font-medium text-gray-900">{selectedDocumentQuery.data.data.name}</p>
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <p className="text-xs uppercase tracking-wide text-gray-400">Status</p>
                      <p className="font-medium text-gray-900">{selectedDocumentQuery.data.data.status}</p>
                    </div>
                    <div>
                      <p className="text-xs uppercase tracking-wide text-gray-400">Visible</p>
                      <p className="font-medium text-gray-900">{selectedDocumentQuery.data.data.visible ? 'Yes' : 'No'}</p>
                    </div>
                    <div>
                      <p className="text-xs uppercase tracking-wide text-gray-400">Stock</p>
                      <p className="font-medium text-gray-900">{selectedDocumentQuery.data.data.stockStatus}</p>
                    </div>
                    <div>
                      <p className="text-xs uppercase tracking-wide text-gray-400">Promotion</p>
                      <p className="font-medium text-gray-900">{selectedDocumentQuery.data.data.hasActivePromotion ? 'Yes' : 'No'}</p>
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <p className="text-xs uppercase tracking-wide text-gray-400">Rating</p>
                      <p className="font-medium text-gray-900">{(selectedDocumentQuery.data.data.ratingAverage ?? 0).toFixed(1)}</p>
                    </div>
                    <div>
                      <p className="text-xs uppercase tracking-wide text-gray-400">Reviews</p>
                      <p className="font-medium text-gray-900">{selectedDocumentQuery.data.data.reviewCount ?? 0}</p>
                    </div>
                  </div>
                  <div>
                    <p className="text-xs uppercase tracking-wide text-gray-400">Indexed at</p>
                    <p className="font-medium text-gray-900">{formatDateTime(selectedDocumentQuery.data.data.indexedAt)}</p>
                  </div>
                  <Button type="button" variant="secondary" className="w-full" onClick={() => handleReindex(selectedProductId)}>
                    Reindex selected
                  </Button>
                </div>
              ) : (
                <div className="text-sm text-gray-500">No document details available.</div>
              )
            ) : (
              <div className="text-sm text-gray-500">Select a document to inspect it.</div>
            )}
          </aside>
        </div>
      )}
    </div>
  );
};

export default AdminSearch;