package domain.customer;

import domain.customer.command.ChangeCustomerEmailAddress;
import domain.customer.command.ChangeCustomerName;
import domain.customer.command.ConfirmCustomerEmailAddress;
import domain.customer.command.RegisterCustomer;
import domain.customer.event.*;
import domain.customer.value.EmailAddress;
import domain.customer.value.Hash;
import domain.customer.value.PersonName;

import java.util.List;

/**
 * This version of a Customer Aggregate is OOP-style, event-sourced, and directly returns the events that have happened.
 * <p>
 * Enable the disabled test cases (remove the @Disabled annotation) in Customer1Test one by one and make them all green!
 * The first test case (RegisterCustomer) is already enabled for you to start.
 * <p>
 * Bonus challenge:
 * What needs to be changed so that the Aggregate keeps it's own state up-to-date, e.g. to be able to handle multiple
 * Commands within one request from the outside?
 * Hint: To test this, you can extend the test cases so that they handle the same command again, resulting in "no changes".
 */
public final class Customer1 {
    private EmailAddress emailAddress;
    private Hash confirmationHash;
    private boolean isEmailAddressConfirmed;
    private PersonName name;

    private Customer1() {
    }

    public static CustomerRegistered register(RegisterCustomer command) {
        return CustomerRegistered.build(
                command.customerID,
                command.emailAddress,
                command.confirmationHash,
                command.name
        );
    }

    public static Customer1 reconstitute(List<Event> events) {
        Customer1 customer = new Customer1();

        customer.apply(events);

        return customer;
    }

    public List<Event> confirmEmailAddress(ConfirmCustomerEmailAddress command) {
        if (!confirmationHash.equals(command.confirmationHash)) {
            return List.of(
                    apply(CustomerEmailAddressConfirmationFailed.build(command.customerID))
            );
        }

        if (isEmailAddressConfirmed) {
            return List.of();
        }

        return List.of(
                apply(
                        CustomerEmailAddressConfirmed.build(command.customerID)
                )
        );
    }

    public List<Event> changeEmailAddress(ChangeCustomerEmailAddress command) {
        if (command.emailAddress.equals(emailAddress)) {
            return List.of();
        }

        return List.of(
                apply(
                        CustomerEmailAddressChanged.build(command.customerID, command.emailAddress, command.confirmationHash)
                )
        );
    }

    public List<Event> changeName(ChangeCustomerName command) {
        if (command.name.equals(name)) {
            return List.of();
        }

        return List.of(
                apply(
                        CustomerNameChanged.build(command.customerID, command.name)
                )
        );
    }

    private void apply(List<Event> events) {
        for (Event event : events) {
            apply(event);
        }
    }

    private Event apply(Event event) {
        if (event.getClass() == CustomerRegistered.class) {
            emailAddress = ((CustomerRegistered) event).emailAddress;
            confirmationHash = ((CustomerRegistered) event).confirmationHash;
            name = ((CustomerRegistered) event).name;
        } else if (event.getClass() == CustomerEmailAddressConfirmed.class) {
            isEmailAddressConfirmed = true;
        } else if (event.getClass() == CustomerEmailAddressChanged.class) {
            emailAddress = ((CustomerEmailAddressChanged) event).emailAddress;
            confirmationHash = ((CustomerEmailAddressChanged) event).confirmationHash;
            isEmailAddressConfirmed = false;
        } else if (event.getClass() == CustomerNameChanged.class) {
            name = ((CustomerNameChanged) event).name;
        }

        return event;
    }
}

