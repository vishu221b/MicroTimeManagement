import React from "react";

function MtmTextArea({ labelText, noOfRows, twStyles, children }) {
  return (
    <label className={twStyles && twStyles.label ? twStyles.label : ""}>
      <span
        className={twStyles && twStyles.labelText ? twStyles.labelText : ""}
      >
        {labelText}
      </span>
      <textarea
        className={twStyles && twStyles.textArea ? twStyles.textArea : ""}
        rows={noOfRows || 2}
      >
        {children}
      </textarea>
    </label>
  );
}

export default MtmTextArea;
