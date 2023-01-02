import React from "react";

function MtmSelect({ labelName, children, twStyles }) {
  let selectKeys = Object.keys(MtmSelect).map((key) => key.toLowerCase());
  return (
    <div>
      <label className={twStyles && twStyles.label ? twStyles.label : ""}>
        <span
          className={twStyles && twStyles.labelText ? twStyles.labelText : ""}
        >
          {labelName}
        </span>
        <select className={twStyles && twStyles.select ? twStyles.select : ""}>
          {children.map((child) => {
            return selectKeys.includes(child.type.name) ? child : null;
          })}
        </select>
      </label>
    </div>
  );
}

const option = ({ children, twStyles, valueName }) => (
  <option
    key={valueName}
    value={valueName}
    className={twStyles && twStyles.option ? twStyles.option : ""}
  >
    {children}
  </option>
);

MtmSelect.Option = option;

export default MtmSelect;
