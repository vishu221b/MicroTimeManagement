import React, { useState } from "react";
import { Link } from "react-router-dom";
import { FiUserPlus } from "react-icons/fi";
import { registerUser } from "../service/ApiService";

const FIELDS = [
  { name: "firstName", label: "First name", type: "text", placeholder: "John", required: true },
  { name: "lastName", label: "Last name", type: "text", placeholder: "Doe" },
  { name: "dateOfBirth", label: "Date of birth", type: "date", max: "2015-12-31" },
  { name: "username", label: "Username", type: "text", placeholder: "johndoe" },
  { name: "email", label: "Email address", type: "email", placeholder: "john@doe.com" },
  { name: "password", label: "Password", type: "password", placeholder: "••••••••" },
];

const emptyForm = FIELDS.reduce((acc, f) => ({ ...acc, [f.name]: "" }), {});

function Registration({ setToastState }) {
  const [form, setForm] = useState(emptyForm);
  const [submitting, setSubmitting] = useState(false);

  const onChange = (e) =>
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setToastState({ display: false });
    setSubmitting(true);

    await registerUser(form, (response, errorResponse) => {
      setSubmitting(false);
      if (response) {
        setToastState({
          display: true,
          variant: "success",
          messages: [response.message],
          includePrefix: true,
          includeSuffix: true,
          suffix: "Redirecting to login…",
        });
        setTimeout(() => (window.location = "/login"), 2000);
        return;
      }
      if (errorResponse && errorResponse.error) {
        const finalErrors = [errorResponse.error.message];
        if (errorResponse.error.errors?.length > 0) {
          errorResponse.error.errors.forEach((err) => finalErrors.push(err));
        }
        setToastState({
          display: true,
          variant: "error",
          messages: finalErrors,
          includePrefix: true,
        });
      }
    });
  };

  return (
    <div className="mtm-flex-1 mtm-flex mtm-items-center mtm-justify-center mtm-px-4 mtm-py-12">
      <div className="ui-card ui-fade-in mtm-w-full mtm-max-w-lg mtm-p-8">
        <div className="mtm-flex mtm-flex-col mtm-items-center mtm-text-center mtm-mb-7">
          <span className="mtm-inline-flex mtm-items-center mtm-justify-center mtm-h-12 mtm-w-12 mtm-rounded-2xl mtm-bg-gradient-to-br mtm-from-primary mtm-to-accent mtm-text-white mtm-mb-4">
            <FiUserPlus size={24} />
          </span>
          <h1 className="mtm-text-2xl mtm-font-display mtm-font-bold mtm-text-content mtm-m-0">
            Create your account
          </h1>
          <p className="mtm-text-sm ui-muted mtm-mt-1 mtm-mb-0">
            Start tracking your time in under a minute.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="mtm-grid mtm-grid-cols-1 sm:mtm-grid-cols-2 mtm-gap-4">
          {FIELDS.map((f) => (
            <div key={f.name} className={f.name === "email" ? "sm:mtm-col-span-2" : ""}>
              <label className="ui-label" htmlFor={f.name}>
                {f.label}
                {f.required ? " *" : ""}
              </label>
              <input
                id={f.name}
                name={f.name}
                type={f.type}
                max={f.max}
                required={f.required}
                placeholder={f.placeholder}
                className="ui-input"
                value={form[f.name]}
                onChange={onChange}
              />
            </div>
          ))}

          <button
            type="submit"
            className="ui-btn ui-btn-primary sm:mtm-col-span-2 mtm-mt-2"
            disabled={submitting}
          >
            {submitting ? <span className="ui-spinner" /> : <FiUserPlus size={18} />}
            {submitting ? "Creating account…" : "Register now"}
          </button>
        </form>

        <p className="mtm-text-sm ui-muted mtm-text-center mtm-mt-6 mtm-mb-0">
          Already have an account?{" "}
          <Link to="/login" className="ui-link">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}

export default Registration;
