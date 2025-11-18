curl --location 'http://localhost:8080/voz-do-povo/publish' \
--header 'Content-Type: application/json' \
--data-raw '{
    "userRequest": {
        "email": "joaosilva@gmail.com"
    },
    "reportAddressRequest": {
        "number": "1020",
        "zipCode": "60175-055",
        "street": "Avenida Santos Dumont",
        "complement": "Apartamento 1203, Bloco B",
        "city": "Fortaleza",
        "state": "CE",
        "country": "Brasil"
    },
    "report": {
        "report": "falta de iluminação na rua",
        "reportDescription": "Iluminação pública insuficiente",
        "reportCategory": "INFRAESTRUTURA"
    }
}'