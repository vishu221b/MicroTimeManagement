import React, { useCallback, useEffect, useState } from "react";
import {
  FiTrash2,
  FiFile,
  FiDownload,
  FiChevronLeft,
  FiChevronRight,
  FiImage,
} from "react-icons/fi";
import FileDropzone from "./FileDropzone";
import { useConfirm } from "./ConfirmProvider";
import { createAttachment, deleteAttachment, listAttachments } from "../service/ApiService";

const readAsDataURL = (file) =>
  new Promise((resolve, reject) => {
    const r = new FileReader();
    r.onload = () => resolve(r.result);
    r.onerror = reject;
    r.readAsDataURL(file);
  });

const isImage = (a) =>
  (a.contentType || "").startsWith("image/") || (a.dataBase64 || "").startsWith("data:image");

const humanSize = (b) =>
  b >= 1048576 ? `${(b / 1048576).toFixed(1)} MB` : b >= 1024 ? `${Math.round(b / 1024)} KB` : `${b || 0} B`;

/**
 * Lists + manages the files attached to a parent entity: a swipeable image
 * carousel, a download list for non-image files, and a drag-drop uploader.
 * `legacyImage` (optional) is a single pre-attachment data-URL shown first.
 */
function AttachmentPanel({ parentType, parentId, legacyImage, onError, onChange }) {
  const [items, setItems] = useState([]);
  const [idx, setIdx] = useState(0);
  const [busy, setBusy] = useState(false);
  const confirm = useConfirm();

  const load = useCallback(() => {
    if (!parentId) return;
    listAttachments({ parentType, parentId }, (data, err) => {
      if (err) return;
      setItems(Array.isArray(data) ? data : []);
    });
  }, [parentType, parentId]);

  useEffect(() => {
    load();
  }, [load]);

  const upload = async (files, error) => {
    if (error) {
      if (onError) onError(error);
      return;
    }
    setBusy(true);
    for (const f of files) {
      // eslint-disable-next-line no-await-in-loop
      const dataUrl = await readAsDataURL(f);
      // eslint-disable-next-line no-await-in-loop
      await new Promise((resolve) =>
        createAttachment(
          { parentType, parentId, name: f.name, contentType: f.type, sizeBytes: f.size, dataBase64: dataUrl },
          (d, err) => {
            if (err && onError) onError((err.error && err.error.message) || "Upload failed.");
            resolve();
          }
        )
      );
    }
    setBusy(false);
    load();
    if (onChange) onChange();
  };

  const remove = async (a) => {
    const ok = await confirm({ title: "Delete file?", message: `Remove "${a.name || "this file"}"?` });
    if (!ok) return;
    deleteAttachment(a.id, (d, err) => {
      if (!err) {
        load();
        if (onChange) onChange();
      }
    });
  };

  const legacy = legacyImage
    ? [{ id: "__legacy__", dataBase64: legacyImage, name: "image", legacy: true }]
    : [];
  const images = [...legacy, ...items.filter(isImage)];
  const files = items.filter((a) => !isImage(a));
  const safeIdx = images.length ? Math.min(idx, images.length - 1) : 0;
  const cur = images[safeIdx];

  return (
    <div className="mtm-flex mtm-flex-col mtm-gap-4">
      {images.length > 0 && (
        <div className="mtm-relative mtm-rounded-xl mtm-border-[3px] mtm-border-ink mtm-overflow-hidden mtm-bg-surface-2">
          <a href={cur.dataBase64} target="_blank" rel="noopener noreferrer">
            <img src={cur.dataBase64} alt={cur.name} className="mtm-w-full mtm-max-h-80 mtm-object-contain" />
          </a>
          {images.length > 1 && (
            <>
              <button
                onClick={() => setIdx((safeIdx - 1 + images.length) % images.length)}
                className="ui-btn ui-btn-ghost ui-btn-sm mtm-absolute mtm-top-1/2 mtm-left-2 -mtm-translate-y-1/2"
                aria-label="Previous"
              >
                <FiChevronLeft />
              </button>
              <button
                onClick={() => setIdx((safeIdx + 1) % images.length)}
                className="ui-btn ui-btn-ghost ui-btn-sm mtm-absolute mtm-top-1/2 mtm-right-2 -mtm-translate-y-1/2"
                aria-label="Next"
              >
                <FiChevronRight />
              </button>
              <span className="ui-chip mtm-text-xs mtm-absolute mtm-bottom-2 mtm-left-1/2 -mtm-translate-x-1/2">
                {safeIdx + 1} / {images.length}
              </span>
            </>
          )}
          {!cur.legacy && (
            <button
              onClick={() => remove(cur)}
              className="ui-btn ui-btn-danger ui-btn-sm mtm-absolute mtm-top-2 mtm-right-2"
              aria-label="Delete image"
            >
              <FiTrash2 size={14} />
            </button>
          )}
        </div>
      )}

      {files.length > 0 && (
        <ul className="mtm-flex mtm-flex-col mtm-gap-2 mtm-list-none mtm-p-0 mtm-m-0">
          {files.map((a) => (
            <li
              key={a.id}
              className="mtm-flex mtm-items-center mtm-gap-3 mtm-p-2.5 mtm-rounded-lg mtm-border-2 mtm-border-ink mtm-bg-surface-2"
            >
              <FiFile className="mtm-text-primary mtm-shrink-0" size={18} />
              <div className="mtm-min-w-0 mtm-flex-1">
                <div className="mtm-font-bold mtm-text-content mtm-truncate">{a.name || "file"}</div>
                <div className="ui-muted mtm-text-xs">
                  {(a.contentType || "file")} · {humanSize(a.sizeBytes)}
                </div>
              </div>
              <a href={a.dataBase64} download={a.name} className="ui-btn ui-btn-ghost ui-btn-sm" aria-label="Download">
                <FiDownload size={14} />
              </a>
              <button onClick={() => remove(a)} className="ui-btn ui-btn-danger ui-btn-sm" aria-label="Delete">
                <FiTrash2 size={14} />
              </button>
            </li>
          ))}
        </ul>
      )}

      {images.length === 0 && files.length === 0 && (
        <p className="ui-muted mtm-text-sm mtm-m-0 mtm-flex mtm-items-center mtm-gap-2">
          <FiImage /> No files yet — drop some below.
        </p>
      )}

      <FileDropzone onFiles={upload} busy={busy} />
    </div>
  );
}

export default AttachmentPanel;
