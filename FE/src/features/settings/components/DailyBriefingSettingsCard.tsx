import { useId } from 'react';
import ToggleSwitch from '@/components/common/ToggleSwitch';
import { cn } from '@/utils/cn';

const DELIVERY_TIMES = ['07:30', '12:30', '18:30'];

interface DailyBriefingSettingsCardProps {
  isEnabled: boolean;
  deliveryTime: string;
  disabled?: boolean;
  onEnabledChange: (isEnabled: boolean) => void;
  onDeliveryTimeChange: (deliveryTime: string) => void;
}

export default function DailyBriefingSettingsCard({
  isEnabled,
  deliveryTime,
  disabled = false,
  onEnabledChange,
  onDeliveryTimeChange,
}: DailyBriefingSettingsCardProps) {
  const toggleId = useId();
  const deliveryTimeGroupName = useId();

  return (
    <section className="flex flex-col gap-5 rounded-md border border-gray-200 bg-white px-5 py-6">
      <h2 className="text-heading-medium text-text-primary">데일리 브리핑</h2>

      <div className="flex items-center justify-between gap-5">
        <h3 className="text-label-medium font-medium text-text-primary">이메일 브리핑 받기</h3>
        <ToggleSwitch
          id={toggleId}
          checked={isEnabled}
          disabled={disabled}
          onChange={(event) => onEnabledChange(event.target.checked)}
          aria-label="데일리 브리핑 이메일 받기"
        />
      </div>

      <div className="border-t border-gray-400 pt-5">
        <div className="flex items-center justify-between gap-5 max-sm:flex-col max-sm:items-start">
          <h3 className="text-label-medium font-medium text-text-primary">받을 시간</h3>
          <fieldset className="flex flex-wrap gap-2.5 px-2.5">
            <legend className="sr-only">브리핑 받을 시간</legend>
            {DELIVERY_TIMES.map((time) => {
              const inputId = `${deliveryTimeGroupName}-${time.replace(':', '')}`;

              return (
                <label
                  key={time}
                  htmlFor={inputId}
                  className={cn(
                    'rounded-full',
                    isEnabled && !disabled ? 'cursor-pointer' : 'cursor-not-allowed',
                  )}
                >
                  <input
                    id={inputId}
                    type="radio"
                    name={deliveryTimeGroupName}
                    value={time}
                    checked={isEnabled && deliveryTime === time}
                    disabled={!isEnabled || disabled}
                    onChange={() => onDeliveryTimeChange(time)}
                    className="peer sr-only"
                  />
                  <span
                    className={cn(
                      'inline-flex h-10 items-center justify-center rounded-full border border-gray-200 bg-white px-3 text-label-large font-normal text-text-primary transition-colors peer-focus-visible:outline peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-primary-500',
                      isEnabled ? 'hover:bg-gray-50' : 'text-gray-500',
                      isEnabled && deliveryTime === time && 'border-primary-600 bg-primary-600',
                    )}
                  >
                    {time}
                  </span>
                </label>
              );
            })}
          </fieldset>
        </div>
        <p className="mt-5 text-label-small font-medium text-text-tertiary">
          {isEnabled
            ? '바꾸면 내일 아침부터 새 시간에 보내드려요.'
            : '브리핑을 켜면 받을 시간을 선택할 수 있어요.'}
        </p>
      </div>
    </section>
  );
}
