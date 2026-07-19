/**
 * EmailChipInput — a tag/chip editor for email addresses.
 *
 * Props:
 *   value    — current list of accepted email strings
 *   onChange — called with the new list whenever a chip is added or removed
 *
 * Behaviour:
 *   • Accepts email on Enter, comma (,), or blur.
 *   • Rejects invalid email format with an inline red error message.
 *   • Rejects duplicates silently.
 *   • Backspace on an empty input removes the last chip.
 *
 * Created by: Adikarthik Gupta C B
 */
import { useState, useRef, KeyboardEvent, FocusEvent } from "react";

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

interface Props {
  value: string[];
  onChange: (next: string[]) => void;
}

export default function EmailChipInput({ value, onChange }: Props) {
  const [draft, setDraft] = useState("");
  const [error, setError] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  function tryCommit(raw: string) {
    const email = raw.trim().replace(/,$/, "").trim();
    if (!email) {
      setDraft("");
      setError(null);
      return;
    }
    if (!EMAIL_RE.test(email)) {
      setError(`"${email}" is not a valid email address`);
      return;
    }
    if (value.includes(email)) {
      // duplicate — silently discard
      setDraft("");
      setError(null);
      return;
    }
    onChange([...value, email]);
    setDraft("");
    setError(null);
  }

  function handleKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter" || e.key === ",") {
      e.preventDefault();
      tryCommit(draft);
      return;
    }
    if (e.key === "Backspace" && draft === "" && value.length > 0) {
      onChange(value.slice(0, -1));
    }
  }

  function handleBlur(_e: FocusEvent<HTMLInputElement>) {
    if (draft.trim()) tryCommit(draft);
  }

  function removeChip(idx: number) {
    onChange(value.filter((_, i) => i !== idx));
  }

  return (
    <div className="flex flex-col gap-1">
      <div
        className="flex flex-wrap gap-1 items-center border border-white/10 rounded-md bg-surface px-2 py-1 min-h-[2.5rem] cursor-text"
        onClick={() => inputRef.current?.focus()}
      >
        {value.map((chip, i) => (
          <span
            key={chip}
            className="flex items-center gap-1 bg-accent/20 text-accent text-sm rounded px-2 py-0.5"
          >
            {chip}
            <button
              type="button"
              aria-label={`Remove ${chip}`}
              className="hover:text-red-400 leading-none"
              onClick={(e) => { e.stopPropagation(); removeChip(i); }}
            >
              ×
            </button>
          </span>
        ))}
        <input
          ref={inputRef}
          data-testid="chip-input"
          type="text"
          className="flex-1 min-w-[12rem] bg-transparent outline-none text-sm py-0.5"
          placeholder={value.length === 0 ? "email@example.com, …" : ""}
          value={draft}
          onChange={(e) => { setDraft(e.target.value); setError(null); }}
          onKeyDown={handleKeyDown}
          onBlur={handleBlur}
        />
      </div>
      {error && (
        <p role="alert" className="text-red-400 text-xs px-1">
          {error}
        </p>
      )}
    </div>
  );
}
