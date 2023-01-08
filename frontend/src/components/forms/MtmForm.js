import React from "react";
import MtmInput from "./MtmInput";
import MtmSelect from "./MtmSelect";
import MtmStyleWrap from "../hoc/MtmStyleWrap";
import MtmTextArea from "./MtmTextArea";

function MtmForm({
  children,
  onFormSubmit,
  method,
  wrapperStyle,
  bgStyle,
  shadowStyle,
  action,
  opacity,
}) {
  return (
    <div
      className={
        wrapperStyle && wrapperStyle.override
          ? wrapperStyle.style
          : `
          mtm-max-w-[90%] 
          sm:mtm-max-w-[80%] 
          md:mtm-max-w-[70%] 
          lg:mtm-max-w-[60%] 
          xl:mtm-max-w-[50%] 
          mtm-mx-auto
          mtm-my-[4%] 
          sm:mtm-mt-[2%] 
          mtm-py-[3%] 
          mtm-border-[0px] 
          mtm-font-sans 
          mtm-tracking-wider 
          ${bgStyle || "mtm-bg-yellow-500/60"}
          ${shadowStyle || "mtm-shadow-yellow-600"}
          mtm-rounded-lg 
          mtm-bg-gradient-to-l 
          mtm-from-black/70
          mtm-shadow-[0px_0px_20px_2px]
          mtm-z-10
          ${opacity || "mtm-opacity-90"}
          ${wrapperStyle && wrapperStyle.style}
          `
      }
    >
      <form
        className="mtm-my-6 mtm-max-w-[80%] mtm-mx-auto"
        onSubmit={onFormSubmit}
        method={method || ""}
        action={action || ""}
      >
        {children}
      </form>
    </div>
  );
}

MtmForm.Input = MtmStyleWrap(MtmInput);
MtmForm.Select = MtmStyleWrap(MtmSelect);
MtmForm.Option = MtmStyleWrap(MtmSelect.Option);
MtmForm.TextArea = MtmStyleWrap(MtmTextArea);

export default MtmForm;
