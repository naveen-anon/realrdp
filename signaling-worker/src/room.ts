export class Room {
  state: DurableObjectState;
  sessions: WebSocket[] = [];

  constructor(state: DurableObjectState) {
    this.state = state;
  }

  async fetch(request: Request): Promise<Response> {
    const upgradeHeader = request.headers.get("Upgrade");
    if (upgradeHeader !== "websocket") {
      return new Response("Expected WebSocket", { status: 426 });
    }

    const pair = new WebSocketPair();
    const [client, server] = Object.values(pair);

    this.handleSession(server);

    return new Response(null, {
      status: 101,
      webSocket: client,
    });
  }

  handleSession(ws: WebSocket) {
    ws.accept();

    if (this.sessions.length >= 2) {
      ws.send(JSON.stringify({ type: "error", message: "Room full" }));
      ws.close();
      return;
    }

    this.sessions.push(ws);

    ws.addEventListener("message", (event: MessageEvent) => {
      for (const session of this.sessions) {
        if (session !== ws && session.readyState === WebSocket.READY_STATE_OPEN) {
          session.send(event.data as string);
        }
      }
    });

    ws.addEventListener("close", () => {
      this.sessions = this.sessions.filter((s) => s !== ws);
    });

    ws.addEventListener("error", () => {
      this.sessions = this.sessions.filter((s) => s !== ws);
    });
  }
}
