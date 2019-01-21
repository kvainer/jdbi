/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbi.v3.core.internal;

import java.sql.Types;
import java.util.Optional;

import org.jdbi.v3.core.Enums;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.jdbi.v3.core.argument.QualifiedArgumentFactory;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.qualifier.QualifiedType;

public class QualifiedEnumArgumentFactory implements QualifiedArgumentFactory {
    @Override
    public Optional<Argument> build(QualifiedType<?> givenType, Object value, ConfigRegistry config) {
        return Optional.of(givenType.getType())
            .map(GenericTypes::getErasedType)
            .filter(Class::isEnum)
            .flatMap(clazz -> makeEnumArgument((QualifiedType<Enum>) givenType, (Enum) value, (Class<Enum>) clazz, config));
    }

    private static <E extends Enum<E>> Optional<Argument> makeEnumArgument(QualifiedType<E> givenType, E value, Class<E> enumClass, ConfigRegistry config) {
        boolean byName = Enums.EnumHandling.BY_NAME == config.get(Enums.class).findStrategy(givenType, enumClass);

        if (value == null) {
            return Optional.of(new NullArgument(byName ? Types.VARCHAR : Types.INTEGER));
        }

        if (byName) {
            return config.get(Arguments.class).findFor(String.class, value.name());
        } else {
            return config.get(Arguments.class).findFor(Integer.class, value.ordinal());
        }
    }
}
