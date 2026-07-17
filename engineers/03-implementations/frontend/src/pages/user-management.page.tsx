import React, { useEffect } from 'react';
import { Renderer, useStateStore } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import spec from '@/schemas/user-management.render-schema.json';
import { useListUsers } from '@/hooks/use-list-users';

export default function UserManagementPage() {
  const store = useStateStore();
  const { data: users } = useListUsers({});

  useEffect(() => {
    if (users) {
      store.set('/data/listUsers', users);
    }
  }, [users, store]);

  return <Renderer spec={spec} registry={componentRegistry} />;
}
