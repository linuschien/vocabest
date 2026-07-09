import React from 'react';
import { useStateStore } from '@json-render/react';

export default function DataTable({ element, children }: any) {
  const store = useStateStore();
  const { id, label, columns, data } = element?.props ?? {};
  
  // Resolve data via store binding
  let rows: any[] = [];
  if (data && data.$bindState) {
    rows = (store.get(data.$bindState) as any[]) || [];
  } else if (Array.isArray(data)) {
    rows = data;
  }

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
            {children && children.length > 0 && <th className="px-6 py-3 text-right">Actions</th>}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr><td colSpan={columns?.length + (children ? 1 : 0)} className="px-6 py-4 text-center">(沒有資料)</td></tr>
          ) : (
            rows.map((row: any, i: number) => (
              <tr key={row.id || i} className="bg-white border-b hover:bg-gray-50">
                {columns?.map((col: any) => (
                  <td key={col.field} className="px-6 py-4 font-medium text-gray-900 whitespace-nowrap">
                    {row[col.field]}
                  </td>
                ))}
                {children && children.length > 0 && (
                  <td className="px-6 py-4 text-right flex justify-end gap-2">
                    {/* Render children (like row actions) here if any */}
                    {children}
                  </td>
                )}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
