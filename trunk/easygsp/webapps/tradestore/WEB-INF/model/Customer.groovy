package model

class Customer {
        private def dynamicProperties = []
        static primaryKeys = ['customerId']

        Long customerId
        String lastname
        String firstname
        String email1
        String email2
        String phone1
        String phone2
}
