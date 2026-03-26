import axios from "axios";

const headers = {
  "content-type": "application/json",
};

const registrationUrl = "http://localhost/mtm-dev/api/v1/users/register";
// const registrationUrl = "http://localhost:8080/mtm-dev/api/v1/users/register";
// const registrationUrl = "http://170.64.128.27/mtm-dev/api/v1/users/register";

export const registerUser = async (data, callback) => {
  axios
    .post(registrationUrl, data, { headers: headers })
    .then((response) => {
      return callback(response.data, null);
    })
    .catch((err) => {
      if (err.name && err.name === "AxiosError" && err.code === "ERR_NETWORK") {
        callback(null, {
          error: {
            message: err.message + ": Please check your internet connection.",
          },
        });
      } else {
        callback(null, err.response.data);
      }
    });
};
