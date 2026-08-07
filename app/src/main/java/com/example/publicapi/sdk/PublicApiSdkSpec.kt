package com.example.publicapi.sdk

object PublicApiSdkSpec {

    fun generateOpenApiSpecJson(): String {
        return """
        {
          "openapi": "3.0.3",
          "info": {
            "title": "SmartBiz Enterprise Public API",
            "description": "Secure Public REST API Platform for SmartBiz ERP. Enables third-party integrations, SaaS tools, and marketplace extensions.",
            "version": "1.0.0",
            "contact": {
              "name": "SmartBiz Developer Support",
              "email": "dev-support@smartbiz.io"
            }
          },
          "servers": [
            {
              "url": "https://api.smartbiz.io/v1",
              "description": "Production API Server"
            },
            {
              "url": "https://sandbox-api.smartbiz.io/v1",
              "description": "Sandbox / Test Server"
            }
          ],
          "paths": {
            "/invoices": {
              "get": {
                "summary": "List Invoices",
                "operationId": "listInvoices",
                "parameters": [
                  { "name": "limit", "in": "query", "schema": { "type": "integer", "default": 20 } },
                  { "name": "page", "in": "query", "schema": { "type": "integer", "default": 1 } }
                ],
                "responses": {
                  "200": { "description": "Successful Response" },
                  "401": { "description": "Unauthorized" },
                  "429": { "description": "Rate Limit Exceeded" }
                }
              },
              "post": {
                "summary": "Create Invoice",
                "operationId": "createInvoice",
                "responses": {
                  "201": { "description": "Invoice Created" }
                }
              }
            },
            "/customers": {
              "get": {
                "summary": "List Customers",
                "operationId": "listCustomers",
                "responses": {
                  "200": { "description": "Successful Response" }
                }
              }
            },
            "/inventory": {
              "get": {
                "summary": "List Inventory Products",
                "operationId": "listInventory",
                "responses": {
                  "200": { "description": "Successful Response" }
                }
              }
            },
            "/analytics/summary": {
              "get": {
                "summary": "Get Business Analytics Summary",
                "operationId": "getAnalyticsSummary",
                "responses": {
                  "200": { "description": "Successful Response" }
                }
              }
            }
          },
          "components": {
            "securitySchemes": {
              "ApiKeyAuth": {
                "type": "apiKey",
                "in": "header",
                "name": "X-API-KEY"
              },
              "OAuth2Bearer": {
                "type": "http",
                "scheme": "bearer",
                "bearerFormat": "JWT"
              }
            }
          },
          "security": [
            { "ApiKeyAuth": [] },
            { "OAuth2Bearer": [] }
          ]
        }
        """.trimIndent()
    }

    fun generateNodeJsSnippet(apiKey: String): String {
        return """
        // Node.js / JavaScript SDK Example
        const axios = require('axios');

        const client = axios.create({
          baseURL: 'https://api.smartbiz.io/v1',
          headers: {
            'X-API-KEY': '$apiKey',
            'Content-Type': 'application/json'
          }
        });

        async function fetchInvoices() {
          try {
            const response = await client.get('/invoices');
            console.log('Invoices:', response.data);
          } catch (error) {
            console.error('API Error:', error.response ? error.response.data : error.message);
          }
        }

        fetchInvoices();
        """.trimIndent()
    }

    fun generateFlutterSnippet(apiKey: String): String {
        return """
        // Flutter / Dart SDK Example
        import 'package:http/http.dart' as http;
        import 'dart:convert';

        class SmartBizClient {
          final String apiKey;
          final String baseUrl;

          SmartBizClient({required this.apiKey, this.baseUrl = 'https://api.smartbiz.io/v1'});

          Future<List<dynamic>> getInvoices() async {
            final response = await http.get(
              Uri.parse('${'$'}baseUrl/invoices'),
              headers: {
                'X-API-KEY': apiKey,
                'Content-Type': 'application/json',
              },
            );

            if (response.statusCode == 200) {
              final json = jsonDecode(response.body);
              return json['data'];
            } else {
              throw Exception('Failed to load invoices: ${'$'}{response.body}');
            }
          }
        }
        """.trimIndent()
    }

    fun generateKotlinSnippet(apiKey: String): String {
        return """
        // Kotlin / Android SDK Example
        import java.net.URL
        import javax.net.ssl.HttpsURLConnection

        class SmartBizPublicSdk(private val apiKey: String) {
            private val baseUrl = "https://api.smartbiz.io/v1"

            fun getInvoices(): String {
                val url = URL("${'$'}baseUrl/invoices")
                val conn = url.openConnection() as HttpsURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("X-API-KEY", apiKey)
                conn.setRequestProperty("Accept", "application/json")

                return conn.inputStream.bufferedReader().use { it.readText() }
            }
        }
        """.trimIndent()
    }
}
