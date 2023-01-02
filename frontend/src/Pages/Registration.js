import axios from "axios";
import React, { useState } from "react";
import { Alert, Container, Toast, ToastContainer } from "react-bootstrap";
import MtmForm from "../components/forms/MtmForm";
import { registerUser } from "../service/ApiService";

function Registration() {
  let defaultToastState = {
    value: false,
    messages: [],
  };
  const [error, setError] = useState(defaultToastState);
  const [success, setSuccess] = useState(defaultToastState);
  const [showError, setShowError] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  return (
    <div
      className="mtm-min-h-screen mtm-p-0  mtm-bg-cover mtm-bg-center"
      style={{
        backgroundImage:
          //   "url('https://gifdb.com/images/high/superhero-batman-under-the-rain-sb576a6je2k3mdet.gif')",
          "url('https://i.gifer.com/100p.gif')",
      }}
    >
      <Container className="mtm-min-w-full mtm-min-h-screen mtm-p-1 mtm-bg-black/70 mtm-z-0">
        <div className="mtm-z-40 mtm-fixed mtm-right-1">
          {error.value ? (
            error.messages && error.messages.length > 0 ? (
              <>
                <Toast
                  delay={2500}
                  animation
                  show={showError}
                  onClick={() => setShowError(false)}
                  autohide={false}
                  className="mtm-bg-red-300/90 mtm-shadow-xl mtm-rounded-xl mtm-shadow-red-500/80 mtm-py-2 mtm-border-red-400 mtm-bordered-2"
                >
                  <Toast.Body
                    className="
                      mtm-text-justify
                      mtm-text-lg 
                      mtm-font-sans 
                      mtm-tracking-wider 
                      mtm-text-red-600
                      mtm-rounded-xl
                      mtm-pl-[16%]
                      "
                  >
                    {error.messages.map((m, i) => (
                      <>
                        <span className="mtm-text-red-700/90">
                          {error.messages.length === 1 ? "Error:" : ""}
                        </span>
                        {i !== 0 ? i + ". " : " "}
                        {m}
                        <br />
                      </>
                    ))}
                  </Toast.Body>
                </Toast>
              </>
            ) : (
              ""
            )
          ) : (
            ""
          )}
          {success.value ? (
            success.messages && success.messages.length > 0 ? (
              <>
                <Toast
                  delay={2500}
                  animation
                  show={showSuccess}
                  onClick={() => setShowSuccess(false)}
                  autohide={false}
                  className="mtm-bg-green-300/90 mtm-shadow-xl mtm-rounded-xl mtm-shadow-green-500/80 mtm-py-2 mtm-border-green-400 mtm-bordered-2"
                >
                  <Toast.Body
                    className="
                      mtm-text-justify 
                      mtm-text-lg 
                      mtm-font-sans 
                      mtm-tracking-wider 
                      mtm-text-green-800
                      mtm-rounded-xl
                      mtm-pl-[16%]
                      "
                  >
                    {success.messages.map((m, i) => (
                      <>
                        <span className="mtm-text-green-700/90">Success: </span>
                        {i !== 0 ? i + ". " : " "}
                        {m}
                        <br />
                      </>
                    ))}
                    Redirecting to Login...
                  </Toast.Body>
                </Toast>
              </>
            ) : (
              ""
            )
          ) : (
            ""
          )}
        </div>

        <MtmForm
          onFormSubmit={async (e) => {
            e.preventDefault();
            setShowSuccess(false);
            setShowError(false);
            let request = {};
            for (let I = 0; I < e.target.length; I++) {
              if (e.target[I].name)
                request[e.target[I].name] = e.target[I].value;
            }
            await registerUser(request, (response, errorResponse) => {
              console.log(response);
              if (response) {
                setSuccess({
                  value: true,
                  messages: [response.message],
                });
                setShowSuccess(true);
                setTimeout(() => (window.location = "/login"), 2000);
              }
              console.log(errorResponse);
              if (errorResponse) {
                let finalErrors = [errorResponse.error.message];
                if (
                  errorResponse.error.errors &&
                  errorResponse.error.errors.length > 0
                ) {
                  errorResponse.error.errors.forEach((e) =>
                    finalErrors.push(e)
                  );
                }
                setError({
                  value: true,
                  alert: "danger",
                  messages: finalErrors,
                });
                setShowError(true);
              }
            });
          }}
        >
          <div className="mtm-text-center mtm-mb-4">
            <span
              style={{ textShadow: "0px 0px 25px green" }}
              className="
            mtm-tracking-widest mtm-text-2xl md:mtm-text-3xl mtm-text-red-500 mtm-underline mtm-animate-pulse"
            >
              {" "}
              Registration
            </span>
          </div>
          <hr className="mtm-max-w-[90%] md:mtm-max-w-full mtm-mx-auto" />
          <div className="mtm-grid mtm-grid-cols-1 mtm-gap-6 mtm-mt-8">
            <MtmForm.Input
              labelName={"First name"}
              type={"text"}
              name={"firstName"}
              placeholder={"John"}
              required
            />
            <MtmForm.Input
              labelName={"Last name"}
              name={"lastName"}
              type={"text"}
              placeholder={"Doe"}
            />
            <MtmForm.Input
              name={"dateOfBirth"}
              labelName={"Date of Birth"}
              type={"date"}
              max={"2015-12-31"}
            />
            <MtmForm.Input
              labelName={"Username"}
              type={"text"}
              name={"username"}
              placeholder={"Doe2211"}
            />
            <MtmForm.Input
              labelName={"Email Address"}
              type={"email"}
              name={"email"}
              placeholder={"john@doe.com"}
            />
            <MtmForm.Input
              name={"password"}
              labelName={"Password"}
              type={"password"}
            />
            <button
              type="submit"
              className="
              mtm-border-0 
              mtm-rounded-lg 
              mtm-w-[30%] 
              mtm-mx-auto 
              mtm-mt-4
              mtm-p-2
              mtm-text-lg 
              sm:mtm-text-xl 
              mtm-ring-1
              mtm-text-white/100
              mtm-bg-green-500 
              hover:mtm-bg-green-600
              active:mtm-bg-green-700
              hover:mtm-ring-1
              mtm-tracking-wider
              "
            >
              Register Now!
            </button>
          </div>
        </MtmForm>
      </Container>
    </div>
  );
}

export default Registration;
