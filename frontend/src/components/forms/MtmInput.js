import React from "react";

function MtmInput({
  type,
  twStyles,
  name,
  placeholder,
  labelName,
  key,
  value,
  id,
  onInputChange,
  children,
  textBefore = true,
  required,
  max,
  min,
}) {
  const inputProps = {
    type: type,
    className: twStyles && twStyles.input ? twStyles.input : "",
    placeholder: placeholder,
    name: name ? name : "",
    onChange: onInputChange,
    required: required,
  };
  if (value) {
    inputProps.value = value;
  }
  if (min) {
    inputProps.min = min;
  }
  if (max) {
    inputProps.max = max;
  }
  return (
    <label
      id={id}
      key={key}
      className={twStyles && twStyles.label ? twStyles.label : ""}
    >
      {textBefore ? (
        <span
          className={twStyles && twStyles.labelText ? twStyles.labelText : ""}
        >
          {labelName}
        </span>
      ) : (
        ""
      )}
      <input {...inputProps} />
      {!textBefore ? (
        <span
          className={twStyles && twStyles.labelText ? twStyles.labelText : ""}
        >
          {labelName}
        </span>
      ) : (
        ""
      )}

      {children}
    </label>
  );
}

export default MtmInput;
