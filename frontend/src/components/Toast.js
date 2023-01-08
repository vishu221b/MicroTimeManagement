import React, { useEffect, useRef, useState } from "react";

function Toast({
  variant,
  includePrefix,
  includeSuffix,
  suffix,
  show,
  autoHide,
  autoHideDelayInMs = 1000,
  children,
}) {
  const [innerShow, setInnerShow] = useState(false);

  const count = useRef(0);

  useEffect(() => {
    if (count.current < 1) {
      setInnerShow(show);
      count.current += 1;
    }
  }, [innerShow, show]);
  if (autoHide) {
    setTimeout(() => {
      setInnerShow(false);
    }, autoHideDelayInMs);
  }

  const prefix = variant === "success" ? "Success: " : "Error: ";

  const colorScheme = {
    success: {
      outerDiv:
        "mtm-bg-green-300/90 mtm-border-green-400 mtm-shadow-green-500/80",
      innerDiv: "mtm-text-green-600",
      span: "mtm-text-green-700/90",
    },
    error: {
      outerDiv: "mtm-bg-red-300/90 mtm-border-red-400 mtm-shadow-red-500/80",
      innerDiv: "mtm-text-red-600",
      span: "mtm-text-red-700/90",
    },
  };
  const finalColor = colorScheme[variant];
  return (
    <>
      <div
        className={`${finalColor.outerDiv} mtm-visible ${
          innerShow
            ? "mtm-visible mtm-delay-750 mtm-transition-all -mtm-translate-x-2 mtm-delay-750 mtm-duration-750 "
            : "mtm-collapse mtm-transition-all mtm-translate-x-96 mtm-delay-100 mtm-duration-500"
        } hover:mtm-cursor-pointer hover:mtm-opacity-95 mtm-break-none mtm-animte-pulse mtm-mt-4 mtm-w-full mtm-shadow-xl mtm-rounded-xl mtm-py-4 mtm-bordered-2`}
      >
        <div
          onClick={() => setInnerShow(false)}
          className="mtm-float-right hover:mtm-ring-2 mtm-mr-4 mtm-rounded-md hover:mtm-scale-110 hover:mtm-duration-100 mtm-ring-gray-500/80"
        >
          <svg
            className="mtm-w-6 mtm-h-6 mtm-opacity-20 hover:mtm-opacity-40"
            viewBox="0 0 24 24"
          >
            <path
              fill="currentColor"
              scale={-10}
              d="M6.4 19L5 17.6l5.6-5.6L5 6.4L6.4 5l5.6 5.6L17.6 5L19 6.4L13.4 12l5.6 5.6l-1.4 1.4l-5.6-5.6Z"
            ></path>
          </svg>
        </div>
        <div
          className={`mtm-text-left mtm-break-words mtm-text-lg mtm-font-sans mtm-tracking-wider mtm-rounded-xl mtm-px-[13%] mtm-my-2 ${finalColor.innerDiv}`}
        >
          <span className={`${finalColor.span}`}>
            {includePrefix ? prefix : ""}
          </span>
          {children}
          {/* <span className={`${finalColor.span}`}> */}
          {includeSuffix ? (
            <>
              <br />
              {suffix}
            </>
          ) : (
            ""
          )}
          {/* </span> */}
          <br />
        </div>
      </div>
    </>
  );
}

export default Toast;
