import React from "react";

function MtmStyleWrap(ComponentToStyle) {
  const processStyleProps = (
    inputStyle,
    processedStyle,
    defaultStyle,
    styleClass
  ) => {
    processedStyle[styleClass] = inputStyle[styleClass].override
      ? inputStyle[styleClass].style
      : `${defaultStyle[styleClass]} ${inputStyle[styleClass].style}`;
  };
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
    let finalStyle = { ...twStyles };
    if (props.twStyles) {
      if (props.twStyles.input) {
        processStyleProps(props.twStyles, finalStyle, twStyles, "input");
      }
      if (props.twStyles.label) {
        processStyleProps(props.twStyles, finalStyle, twStyles, "label");
      }
      if (props.twStyles.labelText) {
        processStyleProps(props.twStyles, finalStyle, twStyles, "labelText");
      }
      if (props.twStyles.select) {
        processStyleProps(props.twStyles, finalStyle, twStyles, "select");
      }
      if (props.twStyles.textArea) {
        processStyleProps(props.twStyles, finalStyle, twStyles, "textArea");
      }
    }
    return <ComponentToStyle {...props} twStyles={finalStyle} />;
  };
}

export default MtmStyleWrap;
