import React from "react";

function MtmStyleWrap(ComponentToStyle) {
  const labelStyle = "mtm-block mtm-tracking-widest";
  const labelTextStyle = "mtm-mx-[5%] md:mtm-mx-auto mtm-text-black/70";
  const inputSelectTextAreaStyle = `
        mtm-font-bal
        mtm-mt-1
        mtm-block
        mtm-w-[90%]
        sm:mtm-w-[90%]
        md:mtm-w-[100%]
        mtm-rounded-md
        mtm-p-2
        mtm-mx-auto
        mtm-bg-gray-100
        mtm-border-transparent
        focus:mtm-border-gray-500 
        focus:mtm-bg-white 
        focus:mtm-ring-0
        mtm-tracking-wider
        `;
  const twStyles = {
    label: labelStyle,
    labelText: labelTextStyle,
    input: inputSelectTextAreaStyle,
    select: inputSelectTextAreaStyle,
    textArea: inputSelectTextAreaStyle,
  };
  return (props) => {
    let finalStyle = twStyles;
    if (props.twStyles) {
      finalStyle = { twStyles, ...props.twStyles };
    }
    return <ComponentToStyle {...props} twStyles={finalStyle} />;
  };
}

export default MtmStyleWrap;
