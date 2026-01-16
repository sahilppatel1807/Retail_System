# Retail system project

This repository tracks my journey learning Spring Boot backend development and react and some other containerization technologies.

## Warehouse

This folder have all of the details to manage the warehouse having the details of the items
### what it can do
The warehouse can list the item and sell it to multiple retailer

## Retailer

This folder have all of the details to manage the retailer having the details of the items in the reatail store where retailer buys the item from warehouse and sell it to customer.

it stores the data of the which customer bough the item.

think it as the wooloworths or the coles market
where it buys from the warehouse and it sale it to the customer making it as a middleman.

## Customer
This folder have all of the details to manage the customer entity having the details of every customer
It stores the details of the customer name, item bought, when it was bough.

## what's done and how it isolated from eachother

in this i added the retailer part where it works on its own
and to comunicate with the warehouse i added the resttemplate

## why customer and customer ui
because it doesnt make any sense to directly connect react ui with the retailer entity as retailer entity has to manage the customer data too,
so because of that i used customer service as the backend, through api ui can get the data of the customer and customer can fetch the data of retailer through api.