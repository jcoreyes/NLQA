__author__ = 'coreyesj'
from flask import Flask
app = Flask(__name__)
@app.route("/")
def hello():
    return "NLQA Test"

if __name__ == "__main__":
    app.run()