import React from "react";
import MtmInput from "./MtmInput";
import MtmSelect from "./MtmSelect";
import MtmStyleWrap from "./MtmStyleWrap";
import MtmTextArea from "./MtmTextArea";

function MtmForm({ children, onFormSubmit, method, wrapperStyle, action }) {
  return (
    <div
      className={
        wrapperStyle ||
        `
    mtm-my-[4%] 
    sm:mtm-mt-[2%] 
    mtm-py-[3%] 
    mtm-border-[0px] 
    mtm-font-sans 
    mtm-tracking-wider 
    mtm-bg-yellow-500/80 
    mtm-rounded-lg 
    mtm-max-w-[90%] 
    sm:mtm-max-w-[80%] 
    md:mtm-max-w-[70%] 
    lg:mtm-max-w-[60%] 
    xl:mtm-max-w-[50%] 
    mtm-mx-auto
    mtm-bg-gradient-to-l mtm-from-black/70
    mtm-shadow-[0px_0px_20px_2px]
    mtm-shadow-yellow-500
    mtm-z-10
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
