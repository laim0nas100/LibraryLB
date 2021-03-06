package lt.lb.commons.email.props;

/**
 *
 * @author laim0nas100
 */
public class POP3EmailProps extends IMAPOrPOP3Props {

    public POP3EmailProps() {

    }

    @Override
    public String getStore() {
        return "pop3";
    }

    @Override
    public void populate() {
        this.put("mail.pop3.port", this.port + "");
    }

    public static class POP3SEmailProps extends POP3EmailProps {

        @Override
        public String getStore() {
            return super.getStore() + "s";
        }
    }
}
