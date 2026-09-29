import { useState } from 'react';
import Tag from '@/components/common/Tag';

interface ProfileAutocompleteTagInputProps {
  options: string[];
  selectedValues: string[];
  onAdd: (value: string) => void;
  placeholder: string;
  ariaLabel: string;
}

export default function ProfileAutocompleteTagInput({
  options,
  selectedValues,
  onAdd,
  placeholder,
  ariaLabel,
}: ProfileAutocompleteTagInputProps) {
  const [isSearching, setIsSearching] = useState(false);
  const [keyword, setKeyword] = useState('');
  const trimmedKeyword = keyword.trim();
  const suggestions = trimmedKeyword
    ? options.filter(
        (option) =>
          option.toLowerCase().includes(trimmedKeyword.toLowerCase()) &&
          !selectedValues.includes(option),
      )
    : [];

  const selectValue = (value: string) => {
    onAdd(value);
    setKeyword('');
    setIsSearching(false);
  };

  if (!isSearching) {
    return (
      <Tag
        variant="add"
        label="검색"
        onClick={() => setIsSearching(true)}
        className="h-10 border-dashed text-label-large text-text-primary"
      />
    );
  }

  return (
    <div className="relative">
      <input
        type="text"
        autoFocus
        value={keyword}
        onChange={(event) => setKeyword(event.target.value)}
        onBlur={() => setIsSearching(false)}
        onKeyDown={(event) => {
          if (event.key === 'Enter' && suggestions[0]) {
            event.preventDefault();
            selectValue(suggestions[0]);
          }
          if (event.key === 'Escape') {
            setKeyword('');
            setIsSearching(false);
          }
        }}
        placeholder={placeholder}
        aria-label={ariaLabel}
        className="h-10 w-36 rounded-full border border-gray-300 bg-white px-3 text-label-large font-normal text-text-primary outline-none focus:border-primary-500"
      />
      {trimmedKeyword && suggestions.length > 0 && (
        <ul className="absolute top-full left-0 z-10 mt-1 flex w-60 flex-col overflow-hidden rounded-md border border-primary-500 bg-white py-1 shadow-md">
          {suggestions.map((option) => (
            <li key={option}>
              <button
                type="button"
                onMouseDown={(event) => event.preventDefault()}
                onClick={() => selectValue(option)}
                className="block w-full px-3 py-2 text-left text-label-medium font-medium text-text-primary hover:bg-gray-100"
              >
                {option}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
