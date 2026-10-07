export class AuthProperties {
  static token: string = "";

  static setProperties(request: Request) {
    AuthProperties.token = request.headers.get("Authorization") || "";
  }

  static getToken() {
    return AuthProperties.token;
  }

  static setToken(token: string) {
    AuthProperties.token = token;
  }
}
