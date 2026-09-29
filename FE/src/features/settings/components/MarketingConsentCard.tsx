import { useId } from 'react';
import ToggleSwitch from '@/components/common/ToggleSwitch';

interface MarketingConsentCardProps {
  isEnabled: boolean;
  disabled?: boolean;
  onEnabledChange: (isEnabled: boolean) => void;
}

export default function MarketingConsentCard({
  isEnabled,
  disabled = false,
  onEnabledChange,
}: MarketingConsentCardProps) {
  const toggleId = useId();

  return (
    <section className="flex flex-col gap-5 rounded-md border border-gray-200 bg-white p-6">
      <h2 className="text-heading-medium text-text-primary">마케팅 · 혜택 소식(별도)</h2>

      <div className="flex items-center justify-between gap-5">
        <div>
          <h3 className="text-label-medium font-medium text-text-primary">
            마케팅 · 이벤트 메일 받기
          </h3>
        </div>
        <ToggleSwitch
          id={toggleId}
          checked={isEnabled}
          disabled={disabled}
          onChange={(event) => onEnabledChange(event.target.checked)}
          aria-label="마케팅 이벤트 메일 받기"
        />
      </div>
      <p className="text-label-small font-medium text-text-tertiary">
        브리핑과 별개예요. 지금은 보내지 않지만, 소식이 생기면 이 설정에 따라 보내드려요.
      </p>
    </section>
  );
}
