
package org.openTwoFactor.server.ws.security;

/* JAAS imports */
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * <p>
 * Pass in 
 * This can be used by a JAAS application to instantiate a
 * CallbackHandler
 * @see javax.security.auth.callback
 */

public class TwoFactorWsKerberosHandler implements CallbackHandler {

  /** principal */
  private String principal;
  
  /** password */
  private String password;
  
  /**
   * <p>Creates a callback handler that prompts and reads from the
   * command line for answers to authentication questions.
   * This can be used by JAAS applications to instantiate a
   * CallbackHandler.
   * @param thePrincipal principal
   * @param thePassword password

   */
  public TwoFactorWsKerberosHandler(String thePrincipal, String thePassword) {
    this.principal = thePrincipal;
    this.password = thePassword;
  }

  /**
   * Handles the specified set of callbacks.
   *
   * @param callbacks the callbacks to handle
   * @throws IOException if an input or output error occurs.
   * @throws UnsupportedCallbackException if the callback is not an
   * instance of NameCallback or PasswordCallback
   */
  public void handle(Callback[] callbacks) throws IOException,
      UnsupportedCallbackException {
    ConfirmationCallback confirmation = null;

    for (int i = 0; i < callbacks.length; i++) {
      if (callbacks[i] instanceof TextOutputCallback) {
        TextOutputCallback tc = (TextOutputCallback) callbacks[i];

        String text;
        switch (tc.getMessageType()) {
          case TextOutputCallback.INFORMATION:
            text = "";
            break;
          case TextOutputCallback.WARNING:
            text = "Warning: ";
            break;
          case TextOutputCallback.ERROR:
            text = "Error: ";
            break;
          default:
            throw new UnsupportedCallbackException(callbacks[i],
                "Unrecognized message type");
        }

        String message = tc.getMessage();
        if (message != null) {
          text += message;
        }

      } else if (callbacks[i] instanceof NameCallback) {
        NameCallback nc = (NameCallback) callbacks[i];

        String result = this.principal;
        if (result.equals("")) {
          result = nc.getDefaultName();
        }

        nc.setName(result);

      } else if (callbacks[i] instanceof PasswordCallback) {
        PasswordCallback pc = (PasswordCallback) callbacks[i];

        pc.setPassword(this.password.toCharArray());

      } else if (callbacks[i] instanceof ConfirmationCallback) {
        confirmation = (ConfirmationCallback) callbacks[i];

      } else {
        throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
      }
    }

    /* Do the confirmation callback last. */
    if (confirmation != null) {
      doConfirmation(confirmation);
    }
  }

  /**
   * 
   * @param confirmation
   * @throws UnsupportedCallbackException
   */
  private void doConfirmation(ConfirmationCallback confirmation) 
      throws UnsupportedCallbackException {
    String prefix;
    int messageType = confirmation.getMessageType();
    switch (messageType) {
      case ConfirmationCallback.WARNING:
        prefix = "Warning: ";
        break;
      case ConfirmationCallback.ERROR:
        prefix = "Error: ";
        break;
      case ConfirmationCallback.INFORMATION:
        prefix = "";
        break;
      default:
        throw new UnsupportedCallbackException(confirmation,
            "Unrecognized message type: " + messageType);
    }

    class OptionInfo {

      /**
       * name
       */
      String name;

      int value;

      /**
       * 
       * @param name1
       * @param value1
       */
      OptionInfo(String name1, int value1) {
        this.name = name1;
        //noop since this.name not read and got warning
        this.value = Integer.parseInt(this.name);
        this.value = value1;
      }
    }

    OptionInfo[] options;
    int optionType = confirmation.getOptionType();
    switch (optionType) {
      case ConfirmationCallback.YES_NO_OPTION:
        options = new OptionInfo[] { new OptionInfo("Yes", ConfirmationCallback.YES),
            new OptionInfo("No", ConfirmationCallback.NO) };
        break;
      case ConfirmationCallback.YES_NO_CANCEL_OPTION:
        options = new OptionInfo[] { new OptionInfo("Yes", ConfirmationCallback.YES),
            new OptionInfo("No", ConfirmationCallback.NO),
            new OptionInfo("Cancel", ConfirmationCallback.CANCEL) };
        break;
      case ConfirmationCallback.OK_CANCEL_OPTION:
        options = new OptionInfo[] { new OptionInfo("OK", ConfirmationCallback.OK),
            new OptionInfo("Cancel", ConfirmationCallback.CANCEL) };
        break;
      case ConfirmationCallback.UNSPECIFIED_OPTION:
        String[] optionStrings = confirmation.getOptions();
        options = new OptionInfo[optionStrings.length];
        for (int i = 0; i < options.length; i++) {
          options[i].value = i;
        }
        break;
      default:
        throw new UnsupportedCallbackException(confirmation, "Unrecognized option type: "
            + optionType);
    }

    int defaultOption = confirmation.getDefaultOption();

    String prompt = confirmation.getPrompt();
    if (prompt == null) {
      prompt = "";
    }
    prompt = prefix + prompt;
    if (!prompt.equals("")) {
      //System.err.println(prompt);
    }

    int result;
    try {
      result = 0;//Integer.parseInt(readLine());
      if (result < 0 || result > (options.length - 1)) {
        result = defaultOption;
      }
      result = options[result].value;
    } catch (NumberFormatException e) {
      result = defaultOption;
    }

    confirmation.setSelectedIndex(result);
  }
}
