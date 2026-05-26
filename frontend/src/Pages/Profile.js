import React, { useEffect, useState } from "react";
import Button from "react-bootstrap/Button";
import {
  changeUserPassword,
  getUserProfile,
  updateUserDetails,
} from "../service/ApiService";

const blankProfile = () => ({
  uid: "",
  username: "",
  email: "",
  firstName: "",
  lastName: "",
  dateOfBirth: "",
});

const errorMessageFrom = (errorPayload) => {
  if (!errorPayload) return "Something went wrong.";
  if (errorPayload.error && errorPayload.error.message)
    return errorPayload.error.message;
  if (errorPayload.message) return errorPayload.message;
  return "Something went wrong.";
};

function Profile({ toastState, setToastState }) {
  const [profile, setProfile] = useState(blankProfile());
  const [loading, setLoading] = useState(true);
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [passwordForm, setPasswordForm] = useState({
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const showSuccess = (message) =>
    setToastState({
      display: true,
      variant: "success",
      messages: [message],
      includePrefix: false,
      includeSuffix: false,
      suffix: "",
    });

  const showError = (messages) =>
    setToastState({
      display: true,
      variant: "error",
      messages: Array.isArray(messages) ? messages : [messages],
      includePrefix: true,
      includeSuffix: false,
      suffix: "",
    });

  useEffect(() => {
    getUserProfile((data, err) => {
      setLoading(false);
      if (err) {
        showError(errorMessageFrom(err));
        return;
      }
      const payload = (data && data.data) || data;
      if (payload) {
        setProfile({
          uid: payload.uid || "",
          username: payload.username || "",
          email: payload.email || "",
          firstName: payload.firstName || "",
          lastName: payload.lastName || "",
          dateOfBirth: payload.dateOfBirth || "",
        });
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleProfileSubmit = (e) => {
    e.preventDefault();
    if (
      !profile.uid ||
      !profile.username ||
      !profile.email ||
      !profile.firstName ||
      !profile.dateOfBirth
    ) {
      showError("Username, email, first name, and date of birth are required.");
      return;
    }
    setSavingProfile(true);
    updateUserDetails(profile, (data, err) => {
      setSavingProfile(false);
      if (err) {
        showError(errorMessageFrom(err));
        return;
      }
      showSuccess("Profile updated.");
    });
  };

  const handlePasswordSubmit = (e) => {
    e.preventDefault();
    if (
      !passwordForm.oldPassword ||
      !passwordForm.newPassword ||
      !passwordForm.confirmPassword
    ) {
      showError("Please fill all password fields.");
      return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      showError("New password and confirmation do not match.");
      return;
    }
    if (passwordForm.newPassword.length < 8) {
      showError("New password must be at least 8 characters long.");
      return;
    }
    setSavingPassword(true);
    changeUserPassword(
      {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
      },
      (data, err) => {
        setSavingPassword(false);
        if (err) {
          showError(errorMessageFrom(err));
          return;
        }
        setPasswordForm({
          oldPassword: "",
          newPassword: "",
          confirmPassword: "",
        });
        showSuccess("Password updated.");
      }
    );
  };

  if (loading) {
    return (
      <div className="mtm-min-h-[60vh] mtm-bg-black mtm-text-white mtm-py-20 mtm-text-center">
        Loading profile…
      </div>
    );
  }

  return (
    <div className="mtm-min-h-[80vh] mtm-bg-black mtm-text-white mtm-py-12 mtm-px-4 sm:mtm-px-12">
      <div className="mtm-max-w-3xl mtm-mx-auto">
        <h1 className="mtm-text-3xl mtm-tracking-wider mtm-text-sky-400 mtm-mb-8">
          My Profile
        </h1>

        <form
          onSubmit={handleProfileSubmit}
          className="mtm-bg-white/5 mtm-border mtm-border-white/20 mtm-rounded mtm-p-6 mtm-mb-8"
        >
          <h2 className="mtm-text-lg mtm-text-yellow-300 mtm-mb-4">
            Account details
          </h2>
          <div className="mtm-grid mtm-gap-4 sm:mtm-grid-cols-2">
            <label className="mtm-flex mtm-flex-col">
              <span className="mtm-text-sm mtm-text-white/70 mtm-mb-1">
                Username
              </span>
              <input
                type="text"
                value={profile.username}
                onChange={(e) =>
                  setProfile((p) => ({ ...p, username: e.target.value }))
                }
                className="mtm-bg-black mtm-border mtm-border-white/30 mtm-rounded mtm-px-3 mtm-py-2"
              />
            </label>
            <label className="mtm-flex mtm-flex-col">
              <span className="mtm-text-sm mtm-text-white/70 mtm-mb-1">
                Email
              </span>
              <input
                type="email"
                value={profile.email}
                onChange={(e) =>
                  setProfile((p) => ({ ...p, email: e.target.value }))
                }
                className="mtm-bg-black mtm-border mtm-border-white/30 mtm-rounded mtm-px-3 mtm-py-2"
              />
            </label>
            <label className="mtm-flex mtm-flex-col">
              <span className="mtm-text-sm mtm-text-white/70 mtm-mb-1">
                First name
              </span>
              <input
                type="text"
                value={profile.firstName}
                onChange={(e) =>
                  setProfile((p) => ({ ...p, firstName: e.target.value }))
                }
                className="mtm-bg-black mtm-border mtm-border-white/30 mtm-rounded mtm-px-3 mtm-py-2"
              />
            </label>
            <label className="mtm-flex mtm-flex-col">
              <span className="mtm-text-sm mtm-text-white/70 mtm-mb-1">
                Last name
              </span>
              <input
                type="text"
                value={profile.lastName}
                onChange={(e) =>
                  setProfile((p) => ({ ...p, lastName: e.target.value }))
                }
                className="mtm-bg-black mtm-border mtm-border-white/30 mtm-rounded mtm-px-3 mtm-py-2"
              />
            </label>
            <label className="mtm-flex mtm-flex-col">
              <span className="mtm-text-sm mtm-text-white/70 mtm-mb-1">
                Date of birth
              </span>
              <input
                type="text"
                placeholder="DD-MM-YYYY"
                value={profile.dateOfBirth}
                onChange={(e) =>
                  setProfile((p) => ({ ...p, dateOfBirth: e.target.value }))
                }
                className="mtm-bg-black mtm-border mtm-border-white/30 mtm-rounded mtm-px-3 mtm-py-2"
              />
            </label>
          </div>
          <div className="mtm-mt-4">
            <Button type="submit" variant="warning" disabled={savingProfile}>
              {savingProfile ? "Saving…" : "Save changes"}
            </Button>
          </div>
        </form>

        <form
          onSubmit={handlePasswordSubmit}
          className="mtm-bg-white/5 mtm-border mtm-border-white/20 mtm-rounded mtm-p-6"
        >
          <h2 className="mtm-text-lg mtm-text-yellow-300 mtm-mb-4">
            Change password
          </h2>
          <div className="mtm-grid mtm-gap-4 sm:mtm-grid-cols-3">
            <label className="mtm-flex mtm-flex-col">
              <span className="mtm-text-sm mtm-text-white/70 mtm-mb-1">
                Current password
              </span>
              <input
                type="password"
                value={passwordForm.oldPassword}
                onChange={(e) =>
                  setPasswordForm((p) => ({
                    ...p,
                    oldPassword: e.target.value,
                  }))
                }
                className="mtm-bg-black mtm-border mtm-border-white/30 mtm-rounded mtm-px-3 mtm-py-2"
              />
            </label>
            <label className="mtm-flex mtm-flex-col">
              <span className="mtm-text-sm mtm-text-white/70 mtm-mb-1">
                New password
              </span>
              <input
                type="password"
                value={passwordForm.newPassword}
                onChange={(e) =>
                  setPasswordForm((p) => ({
                    ...p,
                    newPassword: e.target.value,
                  }))
                }
                className="mtm-bg-black mtm-border mtm-border-white/30 mtm-rounded mtm-px-3 mtm-py-2"
              />
            </label>
            <label className="mtm-flex mtm-flex-col">
              <span className="mtm-text-sm mtm-text-white/70 mtm-mb-1">
                Confirm new password
              </span>
              <input
                type="password"
                value={passwordForm.confirmPassword}
                onChange={(e) =>
                  setPasswordForm((p) => ({
                    ...p,
                    confirmPassword: e.target.value,
                  }))
                }
                className="mtm-bg-black mtm-border mtm-border-white/30 mtm-rounded mtm-px-3 mtm-py-2"
              />
            </label>
          </div>
          <div className="mtm-mt-4">
            <Button type="submit" variant="warning" disabled={savingPassword}>
              {savingPassword ? "Updating…" : "Update password"}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default Profile;
