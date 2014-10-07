__author__ = 'coreyesj'
from flask import Flask, render_template
from flask_bootstrap import Bootstrap
from flask_appconfig import AppConfig
from flask_wtf import Form, RecaptchaField
from wtforms import StringField, TextField, HiddenField, ValidationError, RadioField,\
    BooleanField, SubmitField, IntegerField, FormField, validators
from wtforms.validators import DataRequired

class QuestionForm(Form):
    question = StringField('question', validators=[DataRequired()])

def create_app(configfile=None):
    app = Flask(__name__)
    AppConfig(app, configfile)  # Flask-Appconfig is not necessary, but
                                # highly recommend =)
                                # https://github.com/mbr/flask-appconfig
    Bootstrap(app)

    # in a real app, these should be configured through Flask-Appconfig
    app.config['SECRET_KEY'] = 'devkey'
    app.config['RECAPTCHA_PUBLIC_KEY'] = \
        '6Lfol9cSAAAAADAkodaYl9wvQCwBMr3qGR_PPHcw'

    @app.route('/')
    @app.route('/index', methods=['GET', 'POST'])
    def index():
        form = QuestionForm()
        if form.validate_on_submit():
            print form.question
        return render_template("index.html",
                           form = form)


    return app

if __name__ == '__main__':
    create_app().run(debug=True)