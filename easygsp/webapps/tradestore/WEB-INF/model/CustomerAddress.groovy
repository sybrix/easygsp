package model

class CustomerAddress {
        private def dynamicProperties = []
        static primaryKeys = ['customerAddressId']

        Long customerAddressId
        Long customerId
        String address1
        String address2
        String city
        String state
        String zip
}
