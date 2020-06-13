import os
import string
from datetime import timedelta

from flask import Flask, request, jsonify, abort
import random

from flask_bcrypt import Bcrypt
from flask_jwt import encode_token
from flask_jwt_extended import JWTManager, get_jwt_identity, create_access_token, get_raw_jwt, jwt_required
from flask_sqlalchemy import SQLAlchemy

# Setup the flask-jwt-extended extension. See:
ACCESS_EXPIRES = timedelta(weeks=1)
flask_app = Flask(__name__)

if 'NAMESPACE' in os.environ and os.environ['NAMESPACE'] == 'heroku':
    db_uri = os.environ['DATABASE_URL']
    debug_flag = False
else:  # when running locally: use sqlite
    db_path = os.path.join(os.path.dirname(__file__), 'app.db')
    db_uri = 'sqlite:///{}'.format(db_path)
    debug_flag = True
    flask_app.config['TESTING'] = True

flask_app.config['SQLALCHEMY_DATABASE_URI'] = db_uri
db = SQLAlchemy(flask_app)

bcrypt = Bcrypt(flask_app)  # Inits Bcrypt
flask_app.config['JWT_SECRET_KEY'] = "9ddd8h3f9djd0jd9gf9sg27dg37"
flask_app.config['JWT_BLACKLIST_ENABLED'] = True
flask_app.config['JWT_ACCESS_TOKEN_EXPIRES'] = ACCESS_EXPIRES
flask_app.config['JWT_BLACKLIST_TOKEN_CHECKS'] = ['access']
jwt = JWTManager(flask_app)

chat_messages_tbl = db.Table('messages',
                             db.Column('Chat_id', db.Integer, db.ForeignKey('chat.chat_id'), primary_key=True,
                                       unique=False),
                             db.Column('Message_id', db.String, db.ForeignKey('chat_messages.message_id'),
                                       primary_key=True,
                                       unique=True)
                             )


class User(db.Model):
    def __init__(self, user_id, username, email, passw_hash):
        self.user_id = user_id
        self.username = username
        self.email = email
        self.passw_hash = bcrypt.generate_password_hash(passw_hash).decode('utf-8')

    __tablename__ = 'user'
    user_id = db.Column(db.Integer, primary_key=True, unique=True)
    username = db.Column(db.String(80), unique=True)
    email = db.Column(db.String(120), unique=True)
    passw_hash = db.Column(db.String(200), unique=False, nullable=False)


class Location(db.Model):
    def __init__(self, user_id, location):
        self.user_id = user_id
        self.location = location

    __tablename__ = 'location'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, unique=True)
    location = db.Column(db.String, unique=False)


class PendingFriend(db.Model):
    def __init__(self, sent_by_user_id, received_by_user_id):
        self.sent_by_user_id = sent_by_user_id
        self.received_by_user_id = received_by_user_id

    __tablename__ = 'pendingfriend'
    id = db.Column(db.Integer, primary_key=True)
    sent_by_user_id = db.Column(db.Integer, nullable=False, unique=False)
    received_by_user_id = db.Column(db.Integer, nullable=False, unique=False)


class Friend(db.Model):
    def __init__(self, sent_user_id, accepted_by_user_id):
        self.sent_user_id = sent_user_id
        self.accepted_by_user_id = accepted_by_user_id

    __tablename__ = 'friend'
    id = db.Column(db.Integer, primary_key=True)
    sent_user_id = db.Column(db.Integer, nullable=False, unique=False)
    accepted_by_user_id = db.Column(db.Integer, nullable=False, unique=False)


class Chat(db.Model):
    def __init__(self, chat_id, user_a_id, user_b_id):
        self.chat_id = chat_id
        self.user_a_id = user_a_id
        self.user_b_id = user_b_id

    __tablename__ = 'chat'
    chat_id = db.Column(db.Integer, primary_key=True, unique=True)
    user_a_id = db.Column(db.Integer, nullable=False, unique=False)
    user_b_id = db.Column(db.Integer, nullable=False, unique=False)

    chat_msg_rl = db.relationship('ChatMessage', secondary=chat_messages_tbl, lazy='subquery',
                                  backref=db.backref("chat_messages_tbl", lazy='dynamic'))


class ChatMessage(db.Model):
    def __init__(self, message_id, message, order, sent_by, is_read):
        self.message_id = message_id
        self.message = message
        self.order = order
        self.is_read = is_read
        self.sent_by = sent_by

    __tablename__ = 'chat_messages'
    message_id = db.Column(db.String, primary_key=True, nullable=False, unique=True)
    message = db.Column(db.String(140), nullable=False, unique=False)
    order = db.Column(db.Integer, nullable=False, unique=False)
    sent_by = db.Column(db.Integer, nullable=False)
    is_read = db.Column(db.Boolean, nullable=False)


class Blocked(db.Model):
    def __init__(self, user_id, blocked_user_id):
        self.user_id = user_id
        self.blocked_user_id = blocked_user_id

    __tablename__ = 'blocked'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, nullable=False, unique=False)
    blocked_user_id = db.Column(db.Integer, nullable=False, unique=False)


class Blacklist(db.Model):
    def __init__(self, jti):
        self.jti = jti

    jti = db.Column(db.String(36), primary_key=True, unique=False)


db.create_all()


def get_blocked(blocked_by_user_id, blocked_user_id):
    """Gets a blocked class object."""
    blocked = Blocked.query.filter_by(user_id=blocked_by_user_id, blocked_user_id=blocked_user_id).first()
    return blocked


def get_friend(sent_user_id, received_user_id):
    """Gets a friend class object."""
    friend = Friend.query.filter_by(sent_user_id=sent_user_id,
                                    accepted_by_user_id=received_user_id).first()
    return friend


def get_pending_friend(sent_user_id, received_user_id):
    """Gets a pending friend class object."""
    pending_friend = PendingFriend.query.filter_by(sent_by_user_id=sent_user_id,
                                                   received_by_user_id=received_user_id).first()
    return pending_friend


def random_msg_id():
    """Creates a random string that will be used as a message id."""
    letters = string.ascii_lowercase
    text = ''.join(random.choice(letters + string.digits) for i in range(20))  # Generate a random string
    return text


def chat_msg_count(the_chat_id):
    """Returns the number of messages sent in a chat."""
    the_chat = Chat.query.filter(Chat.chat_id == the_chat_id).first()
    chat_messages_list = the_chat.chat_msg_rl
    if chat_messages_list is None:
        return 1
    else:
        msg_count = len(chat_messages_list) + 1
        return msg_count


def check_if_chat_already_exist(the_user_id, other_user_id):
    """Returns tre if a chat exist otherwise false."""
    a_chat = Chat.query.filter_by(user_a_id=the_user_id,user_b_id=other_user_id ).first()
    second_chat = Chat.query.filter_by(user_a_id=other_user_id, user_b_id=the_user_id).first()
    if a_chat is not None or second_chat is not None:
        return True
    return False  # The chat doesnt already exist


def return_chat_id(the_user_id, other_user_id):
    """Returns the chat id using two different user id's"""
    a_chat = Chat.query.filter_by(user_a_id=the_user_id, user_b_id=other_user_id).first()
    second_chat = Chat.query.filter_by(user_a_id=other_user_id, user_b_id=the_user_id).first()
    if a_chat is not None:
        return a_chat.chat_id
    return second_chat.chat_id


@jwt.token_in_blacklist_loader
def check_if_token_is_revoked(decrypted_token):
    """Checks if the token is revoked or not, returns true if it is revoked"""
    jti = decrypted_token['jti']
    entry = Blacklist.query.filter_by(jti=jti).first()
    if entry is None:
        return False
    return True


@flask_app.route('/register', methods=['POST'])
def register_user():
    """Registers an user"""
    info = request.json
    user = User.query.filter_by(username=info["username"]).first()
    if user is None:
        user = User(user_id=random.randrange(10000), username=info["username"], email=info["email"],
                    passw_hash=info["password"])
        db.session.add(user)
        db.session.commit()
        return jsonify({"message": "Registered"})
    return abort(409)


@flask_app.route('/user/login', methods=['POST'])
def login():
    """Login an user, returns a token"""
    info = request.json
    user = User.query.filter_by(username=info["username"]).first()
    if user is None:
        return abort(401)  # User does not exist
    if bcrypt.check_password_hash(user.passw_hash, info["password"]):
        token = create_access_token(identity=user.user_id)
        return jsonify({"token": token})
    return abort(409)  # Wrong password


@flask_app.route('/user/logout', methods=['DELETE'])
@jwt_required
def logout():
    """Log out an user, blacklists the token"""
    jti = get_raw_jwt()['jti']
    blacklisted = Blacklist(jti=jti)
    db.session.add(blacklisted)
    db.session.commit()
    return jsonify(200)


@flask_app.route('/create_chat', methods=['POST'])
@jwt_required
def create_chat():
    """Creates a chat"""
    if request.method == 'POST':
        the_user_id = get_jwt_identity()  # Its a int
        payload = request.json
        chat_exist = check_if_chat_already_exist(the_user_id, int(payload["userid"]))
        if not chat_exist:
            created_chat_id = random.randrange(10000)
            new_chat = Chat(chat_id=created_chat_id, user_a_id=the_user_id, user_b_id=int(payload["userid"]))
            db.session.add(new_chat)
            db.session.commit()
            return jsonify({"exist": False, "chatid": created_chat_id})
        return jsonify({"exist": True, "chatid": return_chat_id(the_user_id, int(payload["userid"]))})


@flask_app.route('/chats', methods=['GET'])
@jwt_required
def get_chats_username():
    """Retrieves all active chats"""
    if request.method == 'GET':
        user_id = get_jwt_identity()  # Its a int
        username_list = []
        chat_obj_list = Chat.query.filter_by(user_a_id=user_id).all()
        for chat_obj_a in chat_obj_list:
            if chat_obj_a is not None:
                other_person_id = chat_obj_a.user_b_id
                if other_person_id is not None:
                    user = User.query.filter_by(user_id=other_person_id).first()
                    username_list.append(user.username)

        chat_obj_list_b = Chat.query.filter_by(user_b_id=user_id).all()
        for chat_obj_b in chat_obj_list_b:
            if chat_obj_b is not None:
                other_person_id = chat_obj_b.user_a_id
                if other_person_id is not None:
                    user = User.query.filter_by(user_id=other_person_id).first()
                    username_list.append(user.username)
        return jsonify({"list": username_list})


@flask_app.route('/chatmessages', methods=['POST'])
@jwt_required
def get_chat_msgs():
    """Retrieves all chat messages in order from a specific chat"""
    if request.method == 'POST':
        payload = request.json
        message_info = []
        message_order = []
        sorted_message_list = []
        the_chat = Chat.query.filter_by(chat_id=payload["chatid"]).first()
        if the_chat is not None:
            chat_messages_list = the_chat.chat_msg_rl
            if chat_messages_list is not None:
                for chat_message_object in chat_messages_list:
                    message_info.append([chat_message_object.message, chat_message_object.sent_by])
                    message_order.append(chat_message_object.order)
                    sorted_message_list = [x for _, x in sorted(zip(message_order, message_info))]
                return jsonify({"messages": sorted_message_list})
            return {"error": "Empty messagelist"}
        return {"error": "No chat found with that chatid"}  # Chat was not found


@flask_app.route('/chat', methods=['POST'])
@jwt_required
def post_chat_msg():
    """Posts a chat message in a chat"""
    if request.method == 'POST':
        payload = request.json
        the_chat = Chat.query.filter_by(chat_id=payload["chatid"]).first()
        user_id = get_jwt_identity()  # Its a int
        msg_id = random_msg_id()
        order = chat_msg_count(int(payload["chatid"]))  # Retrieves the order the message should be placed in
        new_message = ChatMessage(message_id=msg_id, message=payload["message"], order=order,
                                  sent_by=user_id, is_read=False)
        db.session.add(new_message)
        the_chat.chat_msg_rl.append(new_message)
        db.session.commit()
        return jsonify(200)


@flask_app.route('/user/declinepending', methods=['POST'])
@jwt_required
def decline_friend():
    """Decline a pending friend request the user received"""
    if request.method == 'POST':
        the_user_id = get_jwt_identity()  # The user that received the friend request
        payload = request.json  # Contains user id for the person that sent the request
        pending_friend = get_pending_friend(int(payload["userid"]), the_user_id)
        if pending_friend is not None:
            db.session.delete(pending_friend)
            db.session.commit()
        return jsonify(200)


@flask_app.route('/user/cancelpending', methods=['POST'])
@jwt_required
def cancel_friend():
    if request.method == 'POST':
        """Decline a pending friend request that the user sent to someone"""
        the_user_id = get_jwt_identity()  # The user that sent the friend request
        payload = request.json  # Contains user id for the person that received the request
        pending_friend = get_pending_friend(the_user_id, int(payload["userid"]))
        if pending_friend is not None:
            db.session.delete(pending_friend)
            db.session.commit()
        return jsonify(200)


@flask_app.route('/user/addfriend', methods=['POST'])
@jwt_required
def add_friend():
    """Send a friend request"""
    if request.method == 'POST':
        the_user_id = get_jwt_identity()  # The user that sent the request
        payload = request.json  # Contains user id for the user that received the request
        pending_friend = PendingFriend(sent_by_user_id=the_user_id, received_by_user_id=int(payload["userid"]))
        db.session.add(pending_friend)
        db.session.commit()
        return jsonify(200)


@flask_app.route('/user/acceptfriend', methods=['POST'])
@jwt_required
def accept_friend():
    """Accepts a friend request"""
    if request.method == 'POST':
        accepted_user_id = get_jwt_identity()  # The user that accepts the request
        payload = request.json  # Contains user id for the user that sent the request
        new_friend = Friend(sent_user_id=int(payload["userid"]), accepted_by_user_id=accepted_user_id)
        db.session.add(new_friend)
        db.session.commit()
        return jsonify(200)


@flask_app.route('/pending/sent', methods=['GET'])
@jwt_required
def sent_friend_requests():
    """Retrieve all the sent friend requests"""
    if request.method == 'GET':
        the_user_id = get_jwt_identity()  # The user that sent the friend requests
        pending_friends_obj_list = PendingFriend.query.filter_by(sent_by_user_id=the_user_id).all()
        pending_list = []
        for pending_friend_obj in pending_friends_obj_list:
            received_user_id = pending_friend_obj.received_by_user_id
            user = User.query.filter_by(user_id=received_user_id).first()
            pending_list.append(user.username)
        return jsonify({"sent": pending_list})


@flask_app.route('/pending/received', methods=['GET'])
@jwt_required
def received_friend_requests():
    """Retrieve all the received friend requests"""
    if request.method == 'GET':
        the_user_id = get_jwt_identity()  # The user that received friend requests
        pending_friends_obj_list = PendingFriend.query.filter_by(received_by_user_id=the_user_id).all()
        pending_list = []
        for pending_friend_obj in pending_friends_obj_list:
            sent_user_id = pending_friend_obj.sent_by_user_id
            user = User.query.filter_by(user_id=sent_user_id).first()
            pending_list.append(user.username)
        return jsonify({"received": pending_list})


@flask_app.route('/searchforuser', methods=['POST'])
@jwt_required
def search_for_user():
    """Search for a specific user and return the status between the users e.g are they friends or not"""
    if request.method == 'POST':
        payload = request.json
        searched_user = User.query.filter_by(username=payload["username"]).first()
        if searched_user is not None:  # The user exist
            user_id = get_jwt_identity()  # User that searched for other user

            a_blocked = get_blocked(user_id, searched_user.user_id)
            b_blocked = get_blocked(searched_user.user_id, user_id)
            if a_blocked is not None or b_blocked is not None:
                return jsonify({"userid": searched_user.user_id, "friends": False, "pending": False, "blocked": True})

            sent_friend = get_friend(user_id, searched_user.user_id)
            received_friend = get_friend(searched_user.user_id, user_id)
            if sent_friend is not None or received_friend is not None:
                return jsonify({"userid": searched_user.user_id, "friends": True, "pending": False, "blocked": False})

            sent_pending_friend = get_pending_friend(user_id, searched_user.user_id)
            received_pending_friend = get_pending_friend(searched_user.user_id, user_id)
            if sent_pending_friend is not None or received_pending_friend is not None:
                return jsonify({"userid": searched_user.user_id, "friends": False, "pending": True, "blocked": False})
            return jsonify({"userid": searched_user.user_id, "friends": False, "pending": False, "blocked": False})
        return abort(500)  # User does not exist in the database


@flask_app.route('/getuserid', methods=['POST'])
@jwt_required
def get_userid():
    """Retrieve an user's userid"""
    if request.method == 'POST':
        payload = request.json
        searched_user = User.query.filter_by(username=payload["username"]).first()
        return jsonify({"userid": searched_user.user_id})


@flask_app.route('/removefriend', methods=['POST'])
@jwt_required
def delete_friend():
    """Remove an user as a friend"""
    if request.method == 'POST':
        the_user_id = get_jwt_identity()  # The user that wants to remove friend
        payload = request.json  # The person who will be removed as friend

        a_pending_friend = get_pending_friend(int(payload["userid"]), the_user_id)

        b_pending_friend = get_pending_friend(the_user_id, int(payload["userid"]))

        if a_pending_friend is not None:
            db.session.delete(a_pending_friend)
            db.session.commit()
        if b_pending_friend is not None:
            db.session.delete(b_pending_friend)
            db.session.commit()

        a_current_friend = Friend.query.filter_by(sent_user_id=the_user_id,
                                                  accepted_by_user_id=int(payload["userid"])).first()
        b_current_friend = Friend.query.filter_by(sent_user_id=int(payload["userid"]),
                                                  accepted_by_user_id=the_user_id).first()
        if a_current_friend is not None:
            db.session.delete(a_current_friend)
            db.session.commit()
        if b_current_friend is not None:
            db.session.delete(b_current_friend)
            db.session.commit()
        return jsonify(200)


@flask_app.route('/block', methods=['POST'])
@jwt_required
def block_user():
    """Block an user"""
    if request.method == 'POST':
        the_user_id = get_jwt_identity()  # The user that wants to block a person
        payload = request.json  # The person who will be blocked
        new_block = Blocked(user_id=the_user_id, blocked_user_id=int(payload["userid"]))
        db.session.add(new_block)
        db.session.commit()
        return jsonify(200)


@flask_app.route('/getallblocked', methods=['GET'])
@jwt_required
def get_all_blocks():
    """Retrieve all the blocked users"""
    if request.method == 'GET':
        the_user_id = get_jwt_identity()  # The user that wants to block a person
        blocked_obj_list = Blocked.query.filter_by(user_id=the_user_id).all()
        blocked_list = []
        for blocked_obj in blocked_obj_list:
            user_id = blocked_obj.blocked_user_id  # user id for the person who was blocked
            user = User.query.filter_by(user_id=user_id).first()
            blocked_list.append(user.username)
        return jsonify({"blocked": blocked_list})


@flask_app.route('/unblock', methods=['POST'])
@jwt_required
def unblock_user():
    """Unblock an user"""
    if request.method == 'POST':
        the_user_id = get_jwt_identity()  # The user that wants to unblock a person
        payload = request.json  # The person who will be unblocked
        blocked_person = Blocked.query.filter_by(user_id=the_user_id, blocked_user_id=int(payload["userid"])).first()
        db.session.delete(blocked_person)
        db.session.commit()
        return jsonify(200)


@flask_app.route('/setlocation', methods=['POST'])
@jwt_required
def set_location():
    """Set the logged in user's location"""
    if request.method == 'POST':
        the_user_id = get_jwt_identity()  # The user that wants to set a location
        payload = request.json  # The current user location
        location = Location(user_id=int(the_user_id), location=payload["location"])
        db.session.add(location)
        db.session.commit()
        return jsonify(200)


@flask_app.route('/deletelocation', methods=['POST'])
@jwt_required
def delete_location():
    """Remove an old location of an user"""
    if request.method == 'POST':
        the_user_id = get_jwt_identity()  # The user that will have their old location deleted
        old_location = Location.query.filter_by(user_id=int(the_user_id)).first()
        if old_location is not None:
            db.session.delete(old_location)
            db.session.commit()
        return jsonify(200)


@flask_app.route('/getlocation', methods=['POST'])
@jwt_required
def get_location():
    """Get the location of an user"""
    if request.method == 'POST':
        payload = request.json  # The person who will have their current location retrieved
        location = Location.query.filter_by(user_id=int(payload["userid"])).first()
        if location is not None:
            return jsonify({"location": location.location})
        return jsonify({"location": "None"})


if __name__ == "__main__":
    flask_app.run(debug=True)
