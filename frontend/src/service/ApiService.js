import axios from "axios";

const headers = {
  "content-type": "application/json",
};

const registrationUrl = "/users/register";

export const registerUser = async (data, callback) => {
  console.log("Requesting for:");
  console.log(data);
  axios
    .post(registrationUrl, data, { headers: headers })
    .then((response) => {
      return callback(response.data, null);
    })
    .catch((err) => {
      callback(null, err.response.data);
    });
};
