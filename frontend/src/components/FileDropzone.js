import React, { useRef, useState } from "react";
import { FiUploadCloud } from "react-icons/fi";

const MAX_BYTES = 5 * 1024 * 1024;

/**
 * Rich drag-and-drop upload area. Calls onFiles(files, errorMessage).
 * Rejects anything over 5 MB client-side.
 */
function FileDropzone({ onFiles, accept, multiple = true, hint, busy = false }) {
  const inputRef = useRef(null);
  const [over, setOver] = useState(false);

  const handle = (fileList) => {
    const files = Array.from(fileList || []);
    if (files.length === 0) return;
    const tooBig = files.find((f) => f.size > MAX_BYTES);
    if (tooBig) {
      onFiles([], `"${tooBig.name}" is larger than 5 MB.`);
      return;
    }
    onFiles(files, null);
  };

  return (
    <div
      onDragOver={(e) => {
        e.preventDefault();
        setOver(true);
      }}
      onDragLeave={() => setOver(false)}
      onDrop={(e) => {
        e.preventDefault();
        setOver(false);
        handle(e.dataTransfer.files);
      }}
      onClick={() => !busy && inputRef.current && inputRef.current.click()}
      className={`mtm-cursor-pointer mtm-rounded-xl mtm-border-[3px] mtm-border-dashed mtm-p-6 mtm-text-center mtm-transition-all ${
        over
          ? "mtm-border-primary mtm-bg-primary/10 mtm-scale-[1.01]"
          : "mtm-border-ink/40 mtm-bg-surface-2 hover:mtm-border-primary/60"
      }`}
    >
      <FiUploadCloud className={`mtm-mx-auto mtm-text-3xl mtm-text-primary mtm-mb-2 ${busy ? "mtm-animate-bounce" : ""}`} />
      <div className="mtm-font-bold mtm-text-content">
        {busy ? "Uploading…" : "Drop files here or click to upload"}
      </div>
      <div className="ui-muted mtm-text-sm mtm-mt-1">{hint || "Images & files, up to 5 MB each"}</div>
      <input
        ref={inputRef}
        type="file"
        accept={accept}
        multiple={multiple}
        className="mtm-hidden"
        onChange={(e) => {
          handle(e.target.files);
          e.target.value = "";
        }}
      />
    </div>
  );
}

export default FileDropzone;
