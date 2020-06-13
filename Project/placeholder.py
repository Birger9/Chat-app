import requests
import jwt


def get_payload_user_id(token):
    payload = jwt.decode(token, JWT_SECRET,
                         algorithms=[JWT_ALGORITHM])
    print("Payload identity ", payload['identity'])
    return str(payload['identity'])


JWT_SECRET = "9ddd8h3f9djd0jd9gf9sg27dg37"
JWT_ALGORITHM = 'HS256'

"""
Create a user
Should return a dict containing HTTP 200
"""
user_info = {"username": "Pelle", "password": "wow", "email": "pelle@gmail.com"}

url = "http://127.0.0.1:5000/register"
response = requests.post(url, json=user_info)
print("Pelle register status code: ", response, "RETURN MESSAGE ", response.json())

"""
Create a user
Should return a dict containing HTTP 200
"""
user_info2 = {"username": "Andreas", "password": "wowsig", "email": "andreas@gmail.com"}
response2 = requests.post(url, json=user_info2)
print("Andreas register status code: ", response2, "RETURN MESSAGE ", response2.json())

"""
Create identical user
Should return a dict containing HTTP 409
"""
response80 = requests.post(url, json=user_info2)
print("Andreas register status code: ", response80)

"""
Log in
Should return a dict containing a token
"""
url3 = "http://127.0.0.1:5000/user/login"
response2 = requests.post(url3, json=user_info)
print("Pelle login status code: ", response2, "Login token ", response2.json())

"""
Log in
Should return a dict containing a token
"""
response3 = requests.post(url3, json=user_info2)
print("Andreas login status code: ", response3, "Login token ", response3.json())

"""
Create chat
Should return created chat id and HTTP 200
"""
pelles_token = response2.json()['token']  # Pelles login token
and_token = response3.json()['token']  # Andreas login token
pelle_header = {"Authorization": "Bearer " + pelles_token}

url4 = "http://127.0.0.1:5000/create_chat"
chatidinfo = {"userid": str(get_payload_user_id(and_token))}
response4 = requests.post(url4, headers=pelle_header, json=chatidinfo)
print("First Status Code ", response4, "CREATED CHAT ID ", response4.json()["chatid"])

"""
Pelle posts a chat message in a chat
Should return HTTP 200
"""
chatinfo = {"chatid": str(response4.json()["chatid"]), "message": 'Hej, jag heter Pelle'}
url5 = "http://127.0.0.1:5000/chat"
response5 = requests.post(url5, headers=pelle_header, json=chatinfo)
print("Second Status Code ", response5)

"""
Pelle posts two chat message in the same chat
Should return HTTP 200
"""
chatinfo2 = {"chatid": str(response4.json()["chatid"]), "message": 'Hej, jag heter Pel'}
response6 = requests.post(url5, headers=pelle_header, json=chatinfo2)
print("Third Status Code ", response6)

chatinfo3 = {"chatid": str(response4.json()["chatid"]), "message": 'Hej, jag heter Pe'}
response7 = requests.post(url5, headers=pelle_header, json=chatinfo3)
print("Fourth Status Code ", response7)

"""
Get all messages from a chat, in order. Pelles view
Should return sorted list containing messages
"""
url50 = "http://127.0.0.1:5000/chatmessages"
response8 = requests.post(url50, headers=pelle_header, json=chatinfo)
print("Fifth Status Code ", response8, "SORTED MESSAGES ", response8.json())

"""
Andreas posts a chat message in a chat with Pelle
Should return HTTP 200
"""
and_header = {"Authorization": "Bearer " + and_token}
chatinfo4 = {"chatid": str(response4.json()["chatid"]), "message": 'Hej, jag heter Andreas'}
response9 = requests.post(url5, headers=and_header, json=chatinfo4)
print("Sixth Status Code ", response9)

"""
Get all messages from a chat, in order. Andreas view
Should return sorted list containing messages
"""
response10 = requests.post(url50, headers=and_header, json=chatinfo4)
print("Seventh Status Code ", response10, "SORTED MESSAGES ", response10.json())

"""
Get all currents chats from an user
Should return a list with andreas username
"""
url6 = "http://127.0.0.1:5000/chats"
response11 = requests.get(url6, headers=pelle_header)
print("Eight Status Code ", response11, "CURRENTS CHATS FOR PELLE", response11.json())

"""
Create duplicate chat
Should return chat already created and HTTP 400
"""
useridinfo = {"userid": str(get_payload_user_id(and_token))}
url7 = "http://127.0.0.1:5000/create_chat"
response12 = requests.post(url7, headers=pelle_header, json=useridinfo)
print("Ninth Status Code ", response12, "RETURNED ", response12.json())

"""
Add friend. Pelle adds Andreas
Should return HTTP 200
"""
url8 = "http://127.0.0.1:5000/user/addfriend"
addinfo = {"userid": str(get_payload_user_id(and_token))}
response13 = requests.post(url8, json=addinfo, headers=pelle_header)
print("Tenth Status Code ", response13)

"""
Get all sent  friend requests by Pelle.
Should return a list of added users, containing Andreas username
"""
url9 = "http://127.0.0.1:5000/pending/sent"
response14 = requests.get(url9, headers=pelle_header)
print("Eleventh Status Code ", response14, "SENT FRIEND REQUESTS ", response14.json())

"""
Get all sent friend requests by Andreas.
Should return an empty list
"""
url10 = "http://127.0.0.1:5000/pending/sent"
response15 = requests.get(url10, headers=and_header)
print("Twelfth Status Code ", response15, "SENT FRIEND REQUESTS ", response15.json())

"""
Get all recieved friend requests of Andreas.
Should return a list with Pelles username
"""
url11 = "http://127.0.0.1:5000/pending/received"
response16 = requests.get(url11, headers=and_header)
print("Thirteen Status Code ", response16, "RECEIVED FRIEND REQUESTS ", response16.json())

"""
Log out a user(Andreas)
"""
url12 = "http://127.0.0.1:5000/user/logout"
response17 = requests.delete(url12, headers=and_header)
print("Fourteen Status Code ", response17)

"""
Log in andreas again
Should return a dict containing a token
"""
response18 = requests.post(url3, json=user_info2)
print("Andreas login status code: ", response18, "Login token ", response18.json())
and_token2 = response18.json()['token']
and_header2 = {"Authorization": "Bearer " + and_token2}

"""
Tests user find function
Should return andreas user id
"""
info = {"username": "Andreas"}
url13 = "http://127.0.0.1:5000/searchforuser"
response21 = requests.post(url13, json=info, headers=pelle_header)
print("Seventeen Status Code ", response21, "RETURNED USER ID ", response21.json())

"""
Tests get userid function
Should return andreas user id
"""
info = {"username": "Andreas"}
url14 = "http://127.0.0.1:5000/getuserid"
response22 = requests.post(url14, json=info, headers=pelle_header)
print("Eighteen Status Code ", response22, "RETURNED USER ID ", response22.json())

"""
Tests accept friend request function
Should return HTTP 200
"""
info = {"userid": str(get_payload_user_id(pelles_token))}
url15 = "http://127.0.0.1:5000/user/acceptfriend"
response23 = requests.post(url15, json=info, headers=and_header2)
print("Nineteen Status Code ", response23)

"""
Get all recieved friend requests of Andreas.
Should return a list with Pelles username
"""
response24 = requests.get(url11, headers=and_header2)
print("20 Status Code ", response24, "RECEIVED FRIEND REQUESTS ", response24.json())

"""
Set new location
"""
url16 = "http://127.0.0.1:5000/setlocation"
infoloc = {"location": "Denver"}
response25 = requests.post(url16, json=infoloc, headers=and_header2)
print("21 Status Code ", response24)

"""
Get new location
"""
infostuff = {"userid": str(get_payload_user_id(and_token2))}
url17 = "http://127.0.0.1:5000/getlocation"
response25 = requests.post(url17, json=infostuff, headers=and_header2)
print("22 Status Code ", response25, "CURRENT LOCATION ", response25.json())

"""
Delete a new location
"""
url18 = "http://127.0.0.1:5000/deletelocation"
response26 = requests.post(url18, headers=and_header2)
print("23 Status Code ", response26)

"""
Get new location
"""
response27 = requests.post(url17, json=infostuff, headers=and_header2)
print("24 Status Code ", response27, "CURRENT LOCATION ", response27.json())
