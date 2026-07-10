import React, { useState } from 'react';
import { useStateStore } from '@json-render/react';
import { useNavigate } from 'react-router-dom';

export default function DataTable({ element, children }: any) {
  const store = useStateStore();
  const navigate = useNavigate();
  const { id, label, columns, data, rowActions, pageSize = 10 } = element?.props ?? {};
  
  const [currentPage, setCurrentPage] = useState(1);
  
  // Resolve data via store binding
  let rows: any[] = [];
  if (data && data.$bindState) {
    rows = (store.get(data.$bindState) as any[]) || [];
  } else if (Array.isArray(data)) {
    rows = data;
  }

  const userRole = store.get('/data/user/role');

  const handleActionClick = (action: any, row: any) => {
    store.set('/data/activeWordBank', row);
    store.set('/data/activeWordBankId', row.id);
    
    if (action.type === 'navigate') {
      navigate(action.path);
    } else if (action.type === 'modal') {
      store.set(`/modals/${action.modalId}`, true);
    }
  };

  const totalPages = Math.max(1, Math.ceil(rows.length / pageSize));
  const startIndex = (currentPage - 1) * pageSize;
  const paginatedRows = rows.slice(startIndex, startIndex + pageSize);

  return (
    <div className="border rounded-lg shadow-sm bg-white overflow-hidden" id={id}>
      {label && <div className="px-4 py-3 border-b bg-gray-50 font-semibold text-gray-700">{label}</div>}
      <table className="w-full text-sm text-left text-gray-500">
        <thead className="text-xs text-gray-700 uppercase bg-gray-50">
          <tr>
            {columns?.map((col: any) => (
              <th key={col.field} className="px-6 py-3 cursor-pointer select-none">
                {col.label} {col.sortable ? '↕️' : ''}
              </th>
            ))}
            {(children?.length > 0 || rowActions?.length > 0) && <th className="px-6 py-3 text-right">Actions</th>}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr><td colSpan={columns?.length + (children?.length || rowActions?.length ? 1 : 0)} className="px-6 py-4 text-center">(沒有資料)</td></tr>
          ) : (
            paginatedRows.map((row: any, i: number) => (
              <tr key={row.id || i} className="bg-white border-b hover:bg-gray-50">
                {columns?.map((col: any) => (
                  <td key={col.field} className="px-6 py-4 font-medium text-gray-900 whitespace-nowrap">
                    {row[col.field]}
                  </td>
                ))}
                {(children?.length > 0 || rowActions?.length > 0) && (
                  <td className="px-6 py-4 text-right flex justify-end gap-2 items-center">
                    {children}
                    {rowActions?.map((action: any) => {
                      if (action.adminOnly && userRole !== 'ADMIN') return null;
                      
                      const btnClass = action.variant === 'danger' 
                        ? 'text-red-600 hover:text-red-800' 
                        : 'text-blue-600 hover:text-blue-800';
                        
                      return (
                        <button 
                          key={action.id} 
                          onClick={() => handleActionClick(action, row)}
                          className={`px-3 py-1 text-xs font-medium rounded border ${btnClass} hover:bg-gray-100 transition-colors`}
                        >
                          {action.label}
                        </button>
                      );
                    })}
                  </td>
                )}
              </tr>
            ))
          )}
        </tbody>
      </table>
      
      {rows.length > pageSize && (
        <div className="flex items-center justify-between px-4 py-3 bg-white border-t sm:px-6">
          <div className="flex justify-between flex-1 sm:hidden">
            <button
              onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
              disabled={currentPage === 1}
              className="relative inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50"
            >
              Previous
            </button>
            <button
              onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
              disabled={currentPage === totalPages}
              className="relative ml-3 inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50"
            >
              Next
            </button>
          </div>
          <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
            <div>
              <p className="text-sm text-gray-700">
                Showing <span className="font-medium">{startIndex + 1}</span> to <span className="font-medium">{Math.min(startIndex + pageSize, rows.length)}</span> of <span className="font-medium">{rows.length}</span> results
              </p>
            </div>
            <div>
              <nav className="inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
                <button
                  onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                  disabled={currentPage === 1}
                  className="relative inline-flex items-center px-2 py-2 text-gray-400 rounded-l-md ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
                >
                  <span className="sr-only">Previous</span>
                  <svg className="w-5 h-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                    <path fillRule="evenodd" d="M12.79 5.23a.75.75 0 01-.02 1.06L8.832 10l3.938 3.71a.75.75 0 11-1.04 1.08l-4.5-4.25a.75.75 0 010-1.08l4.5-4.25a.75.75 0 011.06.02z" clipRule="evenodd" />
                  </svg>
                </button>
                <span className="relative inline-flex items-center px-4 py-2 text-sm font-semibold text-gray-900 ring-1 ring-inset ring-gray-300 focus:outline-offset-0">
                  Page {currentPage} of {totalPages}
                </span>
                <button
                  onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                  disabled={currentPage === totalPages}
                  className="relative inline-flex items-center px-2 py-2 text-gray-400 rounded-r-md ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
                >
                  <span className="sr-only">Next</span>
                  <svg className="w-5 h-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                    <path fillRule="evenodd" d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z" clipRule="evenodd" />
                  </svg>
                </button>
              </nav>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
