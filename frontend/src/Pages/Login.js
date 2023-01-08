import React from "react";
import Button from "../components/Button";
import MtmForm from "../components/forms/MtmForm";

function Login({ toastState, setToastState }) {
  return (
    <div
      className="mtm-min-h-full mtm-bg-cover mtm-bg-center mtm-bg-no-repeat sm:mtm-min-h-full"
      style={{
        backgroundImage:
          "url('https://media0.giphy.com/media/l0NwGpoOVLTAyUJSo/giphy.gif')",
      }}
    >
      <div className="mtm-py-20 md:mtm-py-15 mtm-bg-black/60">
        <MtmForm
          bgStyle={"mtm-bg-green-400/20"}
          shadowStyle={"mtm-shadow-green-600"}
          opactity={"mtm-opacity-90"}
          wrapperStyle={{
            style: `
          mtm-py-[3%]
          
          `,
            override: false,
          }}
          onFormSubmit={(e) => {
            e.preventDefault();
            setToastState({ display: false });
            setTimeout(() => {
              setToastState({
                display: true,
                messages: ["Working Now!!"],
                variant: "success",
              });
            }, 1000);
          }}
        >
          <div className="mtm-grid mtm-grid-cols-1 mtm-gap-8 mtm-mt-8 mtm-text-white/80">
            <span
              className="mtm-text-3xl mtm-tracking-wider md:mtm-text-4xl mtm-mx-auto mtm-text-sky-400 mtm-animate-pulse"
              style={{
                textShadow: "0px 0px 30px green",
              }}
            >
              Portal Login
            </span>
            <hr />
            <MtmForm.Input
              twStyles={{
                input: {
                  style: "mtm-my-2",
                  override: false,
                },
                labelText: {
                  style:
                    "mtm-tracking-widest mtm-text-lg mtm-mx-[5%] md:mtm-mx-auto",
                  override: true,
                },
              }}
              labelName={"MTM Username"}
              name={"username"}
              required
              placeholder={"username"}
              type={"text"}
            />
            <MtmForm.Input
              twStyles={{
                input: {
                  style: "mtm-my-2",
                  override: false,
                },
                labelText: {
                  style:
                    "mtm-tracking-widest mtm-text-lg sm:mtm-text-xl mtm-mx-[5%] md:mtm-mx-auto",
                  override: true,
                },
              }}
              labelName={"Mtm Password"}
              name={"password"}
              required
              placeholder={"password"}
              type={"password"}
            />
            <Button
              label={"Access !!"}
              type={"submit"}
              bgStyle="mtm-bg-yellow-500 hover:mtm-bg-yellow-600 active:mtm-bg-yellow-700 mtm-text-black/60"
            />
          </div>
        </MtmForm>
      </div>
    </div>
  );
}

export default Login;
