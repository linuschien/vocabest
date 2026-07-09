import React from 'react';

export default function Breadcrumb({ element, children }: any) {
  return (
    <nav aria-label="Breadcrumb" id={element?.props?.id} className="mb-4">
      <ol className="flex items-center space-x-2 text-sm text-gray-500">
        <li><a href="#" className="hover:text-gray-900">Home</a></li>
        {children && children.map((child: any, idx: number) => (
          <React.Fragment key={idx}>
            <li><span className="mx-2">/</span></li>
            <li>{child}</li>
          </React.Fragment>
        ))}
      </ol>
    </nav>
  );
}
