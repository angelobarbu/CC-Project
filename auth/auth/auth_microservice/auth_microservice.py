# auth_microservice.py

from flask import Flask, request, jsonify, make_response
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_apscheduler import APScheduler
import jwt
from datetime import datetime, timedelta
from functools import wraps
import os

class Config:
    """Config class for Flask app"""
    SCHEDULER_API_ENABLED = True
    # SQLALCHEMY_DATABASE_URI = 'postgresql://postgres:postgres@localhost/authdb' # For local testing
    SQLALCHEMY_DATABASE_URI = 'postgresql://user:password@postgresql-db-service:5432/postgres_db'
    SECRET_KEY = os.urandom(24)
    

app = Flask(__name__)
# app.config['SQLALCHEMY_DATABASE_URI'] = 'postgresql://user:password@localhost/authdb'
app.config.from_object(Config())

scheduler = APScheduler()
scheduler.init_app(app)
scheduler.start()

db = SQLAlchemy(app)
bcrypt = Bcrypt(app)

class User(db.Model):
    """User model"""
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    password = db.Column(db.String(120), nullable=False)
    role = db.Column(db.String(50), nullable=False)
    
class Token(db.Model):
    """Token model"""
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    token = db.Column(db.String(500), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    is_active = db.Column(db.Boolean, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    

def token_required(f):
    """Token verify method"""
    @wraps(f)
    def decorated(*args, **kwargs):
        # Check if token is provided
        token = request.headers.get('Authorization')

        if not token:
            return jsonify({'message': 'Token is missing'}), 401

        # Check if token is valid
        try:
            data = jwt.decode(jwt=token, key=app.config['SECRET_KEY'], algorithms=['HS256'])
            stored_token = Token.query.filter_by(token=token, is_active=True).join(User).filter(User.username == data['username']).first()
            if stored_token is None:
                raise RuntimeError("Token is inactive or not found in database")
            current_user = User.query.get(stored_token.user_id)
        except jwt.ExpiredSignatureError:
            return jsonify({'message': f'Token has expired'}), 401
        except jwt.InvalidTokenError:
            return jsonify({'message': f'Invalid token'}), 401
        except RuntimeError as e:
            return jsonify({'message': f'Runtime error: {e}'}), 401
        except Exception as e:
            return jsonify({'message': f'Token decoding error: {e}'}), 401
        return f(current_user, *args, **kwargs)

    return decorated

def deactivate_old_tokens():
    """Token deactivation method"""
    try:
        expiration_time = datetime.utcnow() - timedelta(hours=2)
        Token.query.filter(Token.created_at < expiration_time).update({'is_active': False})
        db.session.commit()
    except Exception as e:
        db.session.rollback()
        print("Failed to deactivate tokens: ", e)
        
# Schedule token deactivation every 2 hours
scheduler.add_job(id='deactivate_old_tokens', func=deactivate_old_tokens, trigger='interval', hours=2)

@app.route('/db_check', methods=['GET'])
def db_check():
    """Database connection check method"""
    try:
        db.session.query("1").from_statement("SELECT 1").all()
        return jsonify({'status': 'success', 'message': 'Database connection successful'}), 200
    except Exception as e:
        return jsonify({'status': 'error', 'message': f'Database connection failed: {e}'}), 500

@app.route('/register', methods=['POST'])
def register():
    """User registstration method"""
    data = request.get_json()
    
    # Check if username already exists
    user = User.query.filter_by(username=data['username']).first()
    if user:
        return jsonify({'message': 'Username already exists'})
    
    # Hash password
    hashed_password = bcrypt.generate_password_hash(data['password']).decode('utf-8')

    new_user = User(username=data['username'], password=hashed_password, role='user')
    
    # Add user to database
    try:
        db.session.add(new_user)
        db.session.commit()
        
        # Verify if user has been added
        verify_user = User.query.filter_by(username=data['username']).first()
        if verify_user:
            return jsonify({'message': 'User created successfully'}), 201
        else:
            return jsonify({'message': 'Failed to create user'}), 500
    except Exception as e:
        db.session.rollback()
        return jsonify({'message': 'An error occurred: ' + str(e)}), 500

@app.route('/login', methods=['POST'])
def login():
    auth = request.authorization

    # Check if credentials are provided
    if not auth or not auth.username or not auth.password:
        return make_response('Could not verify', 401, {'WWW-Authenticate': 'Basic realm="Login required!"'})

    # Check if user exists and password is correct
    user = User.query.filter_by(username=auth.username).first()

    if not user or not bcrypt.check_password_hash(user.password, auth.password):
        return make_response('Could not verify', 401, {'WWW-Authenticate': 'Basic realm="Login required!"'})

    # Login and generate session token
    token = jwt.encode(payload={'username': user.username, 'exp': datetime.utcnow() + timedelta(hours=1)}, key=app.config['SECRET_KEY'], algorithm='HS256')
    print(token)
    
    # Add token to database
    new_token = Token(token=token, user_id=user.id, is_active=True)
    try:
        db.session.add(new_token)
        db.session.commit()
    except Exception as e:
        db.session.rollback()
        return jsonify({'message': 'Failed to add token to database: ' + str(e)}), 500
    
    return jsonify({'token': token})


@app.route('/protected', methods=['GET'])
@token_required
def protected(current_user):
    # Check user access using token
    return jsonify({'message': f'Welcome, {current_user.username}! You have access to this protected resource.'})

if __name__ == '__main__':
    with app.app_context():
        # Check if connection to database is successful
        try:
            db.create_all()
            print("Connected to the database successfully.")
        except Exception as e:
            print("Failed to connect to the database: ", e)
    app.run(debug=True)
