import React, { useRef } from "react";
import Container from "react-bootstrap/Container";
import Button from "../components/Button";
import MtmForm from "../components/forms/MtmForm";
import { registerUser } from "../service/ApiService";

function Registration({ toastState, setToastState }) {
  // let defaultToastState = {
  //   display: false,
  //   variant: "",
  //   messages: [],
  //   includePrefix: false,
  //   includeSuffix: false,
  //   suffix: "",
  // };
  const refCollection = {
    firstName: useRef(),
    lastName: useRef(),
    dateOfBirth: useRef(),
    username: useRef(),
    email: useRef(),
    password: useRef(),
  };
  // const [toastState, setToastState] = useState(defaultToastState);
  return (
    <div
      className="mtm-min-h-screen mtm-p-0  mtm-bg-cover mtm-bg-center"
      style={{
        backgroundImage: "url('https://i.gifer.com/100p.gif')",
      }}
    >
      <Container className="mtm-min-w-full mtm-min-h-screen mtm-p-1 mtm-bg-black/70 mtm-z-0">
        {/* <div className="mtm-z-40 mtm-w-[60%] sm:mtm-w-[45%] md:mtm-w-[35%] lg:mtm-w-[25%] mtm-fixed mtm-right-1 mtm-flex mtm-flex-col mtm-pr-0">
          {toastState.display ? (
            toastState.messages.map((message, index) => (
              <Toast
                variant={toastState.variant}
                key={index}
                show={true}
                autoHide
                autoHideDelayInMs={5000}
                includePrefix={toastState.variant === "success" ? true : false}
                includeSuffix={toastState.includeSuffix}
                suffix={toastState.suffix}
              >
                {message}
              </Toast>
            ))
          ) : (
            <></>
          )}
        </div> */}

        <MtmForm
          onFormSubmit={async (e) => {
            e.preventDefault();
            setToastState({
              display: false,
            });
            let request = {};
            Object.keys(refCollection).forEach((ref) => {
              request[ref] =
                refCollection[ref].current && refCollection[ref].current.value
                  ? refCollection[ref].current.value
                  : "";
            });
            // console.log(request);
            // for (let I = 0; I < e.target.length; I++) {
            //   if (e.target[I].name)
            //     request[e.target[I].name] = e.target[I].value;
            // }
            await registerUser(request, (response, errorResponse) => {
              console.log(response);
              if (response) {
                setToastState({
                  display: true,
                  variant: "success",
                  messages: [response.message],
                  includePrefix: true,
                  includeSuffix: true,
                  suffix: "Redirecting to Login...",
                });
                setTimeout(() => (window.location = "/login"), 2000);
              }
              console.log(errorResponse);
              if (errorResponse && errorResponse.error) {
                let finalErrors = [errorResponse.error.message];
                if (
                  errorResponse.error.errors &&
                  errorResponse.error.errors.length > 0
                ) {
                  errorResponse.error.errors.forEach((e) =>
                    finalErrors.push(e)
                  );
                }
                setToastState({
                  display: true,
                  variant: "error",
                  messages: finalErrors,
                  includePrefix: true,
                });
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
              inputRef={refCollection.firstName}
              labelName={"First name"}
              type={"text"}
              name={"firstName"}
              placeholder={"John"}
              required
            />
            <MtmForm.Input
              inputRef={refCollection.lastName}
              labelName={"Last name"}
              name={"lastName"}
              type={"text"}
              placeholder={"Doe"}
            />
            <MtmForm.Input
              inputRef={refCollection.dateOfBirth}
              name={"dateOfBirth"}
              labelName={"Date of Birth"}
              type={"date"}
              max={"2015-12-31"}
            />
            <MtmForm.Input
              inputRef={refCollection.username}
              labelName={"Username"}
              type={"text"}
              name={"username"}
              placeholder={"Doe2211"}
            />
            <MtmForm.Input
              inputRef={refCollection.email}
              labelName={"Email Address"}
              type={"email"}
              name={"email"}
              placeholder={"john@doe.com"}
            />
            <MtmForm.Input
              inputRef={refCollection.password}
              name={"password"}
              labelName={"Password"}
              type={"password"}
            />
            <Button type={"submit"} label={"Register Now!"} bgColor={"green"} />
          </div>
        </MtmForm>
      </Container>
    </div>
  );
}

export default Registration;
