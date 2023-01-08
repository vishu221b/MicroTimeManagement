import React from "react";

function Button({ type, label, bgColor, bgStyle }) {
  const bgColors = {
    green: "mtm-bg-green-500 hover:mtm-bg-green-600 active:mtm-bg-green-700",
    red: "mtm-bg-red-500 hover:mtm-bg-red-600 active:mtm-bg-red-700",
    sky: "mtm-bg-sky-500 hover:mtm-bg-sky-600 active:mtm-bg-sky-700",
    yellow:
      "mtm-bg-yellow-500 hover:mtm-bg-yellow-600 active:mtm-bg-yellow-700",
  };
  return (
    <button
      type={type}
      className={`
              mtm-border-0 
              mtm-rounded-lg 
              mtm-w-[30%] 
              mtm-mx-auto
              mtm-mt-4
              mtm-p-2
              mtm-text-md 
              sm:mtm-text-xl 
              mtm-ring-1
              mtm-text-white/100
              ${bgStyle || bgColors[bgColor || "green"]}
              hover:mtm-ring-1
              mtm-tracking-wider
              `}
    >
      {label}
    </button>
  );
}

export default Button;
