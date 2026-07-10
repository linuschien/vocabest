import React, { useEffect, useState, useRef } from 'react';
import { Renderer, useStateStore, useStateValue } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import spec from '@/schemas/vocabulary-dictionary.render-schema.json';
import { useListWordBanks } from '@/hooks/use-list-word-banks';

export default function VocabularyDictionaryPage() {
  const store = useStateStore();
  const [activeFilter, setActiveFilter] = useState<any>(undefined);
  
  // Refetch is extracted so we can force a refetch if needed
  const { data: wordBanks, refetch } = useListWordBanks(activeFilter);

  useEffect(() => {
    // Set initial form defaults if they are undefined
    if (store.get('/form/letter-select') === undefined) store.set('/form/letter-select', 'Any');
    if (store.get('/form/difficulty-select') === undefined) store.set('/form/difficulty-select', 'Any');

    // Register the search trigger action in the global store.
    // This allows BehaviorProvider to invoke it when the "Search" button is clicked.
    store.set('/actions/triggerSearch', (overridePage?: number) => {
      const searchField = store.get('/form/search-field');
      const letterSelect = store.get('/form/letter-select');
      const difficultySelect = store.get('/form/difficulty-select');
      
      // If overridePage is provided (e.g., from pagination), use it.
      // Otherwise (e.g., from search button), reset to page 1.
      const page = overridePage !== undefined ? overridePage : 1;
      
      // Ensure the store is synced if we reset to page 1
      if (overridePage === undefined) {
        store.set('/form/page', 1);
      }
      
      const filter: any = {
        page: page - 1,
        size: 10 // Page size 10 to match UI
      };
      if (searchField) filter.word = searchField;
      if (letterSelect && letterSelect !== "Any") filter.startingLetter = letterSelect;
      if (difficultySelect && difficultySelect !== "Any") filter.difficultyLevel = parseInt(difficultySelect as string, 10);
      
      setActiveFilter(filter);
    });

    store.set('/actions/clearSearch', () => {
      store.set('/form/search-field', '');
      store.set('/form/letter-select', 'Any');
      store.set('/form/difficulty-select', 'Any');
      store.set('/form/page', 1);
      
      setTimeout(() => {
        const trigger = store.get('/actions/triggerSearch') as (page: number) => void;
        if (trigger) trigger(1);
      }, 0);
    });

    store.set('/actions/pageChange', (params: any) => {
      // Handle both { page: 2 } and raw number 2 due to schema parsing limitations
      const newPage = typeof params === 'number' ? params : (params?.page || 1);
      store.set('/form/page', newPage);
      const trigger = store.get('/actions/triggerSearch') as (page: number) => void;
      if (trigger) trigger(newPage);
    });
    
    // Explicitly initialize the active filter with the correct page size
    const trigger = store.get('/actions/triggerSearch') as (page: number) => void;
    if (trigger) trigger(1);
    
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // IMPORTANT: Empty dependency array prevents infinite loops!

  const targetLevel = useStateValue('/data/user/targetLevel');
  const prevTargetLevelRef = useRef(targetLevel);
  
  useEffect(() => {
    // Only run this if the targetLevel actually changes
    if (targetLevel !== prevTargetLevelRef.current) {
      prevTargetLevelRef.current = targetLevel;
      
      // Whenever the user's targetLevel changes, reset page to 1 and trigger a refetch
      // so the backend can apply the new mandatory filter from the first page.
      store.set('/form/page', 1);
      const trigger = store.get('/actions/triggerSearch') as (page: number) => void;
      if (trigger) {
        trigger(1);
      } else {
        refetch();
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [targetLevel]);

  useEffect(() => {
    if (wordBanks && (wordBanks as any).content) {
      store.set('/data/listWordBanks', (wordBanks as any).content);
      store.set('/data/listWordBanksTotal', (wordBanks as any).totalElements);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [wordBanks]); // IMPORTANT: Do not include `store` to prevent infinite loops!

  return <Renderer spec={spec} registry={componentRegistry} />;
}
